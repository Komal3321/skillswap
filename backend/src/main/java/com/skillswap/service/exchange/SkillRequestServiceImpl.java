package com.skillswap.service.exchange;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import com.skillswap.common.exception.BadRequestException;
import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.domain.entity.Skill;
import com.skillswap.domain.entity.SkillRequest;
import com.skillswap.domain.entity.Role.RoleName;
import com.skillswap.domain.entity.User;
import com.skillswap.domain.entity.UserSkill.SkillType;
import com.skillswap.domain.enums.ExchangeType;
import com.skillswap.domain.enums.RequestStatus;
import com.skillswap.dto.request.exchange.CreateSkillRequestDTO;
import com.skillswap.dto.request.exchange.UpdateSkillRequestDTO;
import com.skillswap.dto.response.exchange.RequestHistoryDTO;
import com.skillswap.dto.response.exchange.SkillMatchResponseDTO;
import com.skillswap.dto.response.exchange.SkillRequestResponseDTO;
import com.skillswap.repository.SkillRepository;
import com.skillswap.repository.SkillRequestRepository;
import com.skillswap.repository.UserRepository;
import com.skillswap.repository.UserSkillRepository;
import com.skillswap.security.user.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of skill exchange request workflows.
 */
@Service
public class SkillRequestServiceImpl implements SkillRequestService {

    private static final Duration REQUEST_TTL = Duration.ofDays(7);
    private static final String STATUS_CACHE_PREFIX = "skill-request:status:";
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final Map<String, String> SORT_FIELDS = Map.of(
            "id", "id",
            "status", "status",
            "createdAt", "createdAt",
            "updatedAt", "updatedAt",
            "expiresAt", "expiresAt");

    private final SkillRequestRepository skillRequestRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final StringRedisTemplate redisTemplate;

    public SkillRequestServiceImpl(
            SkillRequestRepository skillRequestRepository,
            UserRepository userRepository,
            SkillRepository skillRepository,
            UserSkillRepository userSkillRepository,
            StringRedisTemplate redisTemplate) {
        this.skillRequestRepository = skillRequestRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional
    public SkillRequestResponseDTO createRequest(CreateSkillRequestDTO request) {
        User learner = findUser(currentUserId());
        User mentor = findUser(request.mentorId());
        if (learner.getId().equals(mentor.getId())) {
            throw new BadRequestException("Learner and mentor must be different users");
        }
        validateMentorAccount(mentor);

        Skill requestedSkill = findSkill(request.requestedSkillId());
        Skill offeredSkill = request.offeredSkillId() == null ? null : findSkill(request.offeredSkillId());
        validateSchedule(request.requestedStartTime(), request.requestedEndTime());
        validateExchangeType(request.exchangeType(), offeredSkill);
        validateMentorCanTeach(mentor.getId(), requestedSkill.getId());
        validateLearnerCanOffer(learner.getId(), offeredSkill);

        if (skillRequestRepository.existsByRequesterIdAndProviderIdAndSkillIdAndStatusIn(
                learner.getId(),
                mentor.getId(),
                requestedSkill.getId(),
                Set.of(RequestStatus.PENDING, RequestStatus.ACCEPTED))) {
            throw new BadRequestException("Duplicate active request already exists for this mentor and skill");
        }

        SkillRequest skillRequest = SkillRequest.builder()
                .requester(learner)
                .provider(mentor)
                .skill(requestedSkill)
                .offeredSkill(offeredSkill)
                .exchangeType(request.exchangeType())
                .message(normalizeNullable(request.message()))
                .requestedStartTime(request.requestedStartTime())
                .requestedEndTime(request.requestedEndTime())
                .status(RequestStatus.PENDING)
                .expiresAt(Instant.now().plus(REQUEST_TTL))
                .build();
        SkillRequest saved = skillRequestRepository.save(skillRequest);
        trackStatus(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public SkillRequestResponseDTO acceptRequest(Long requestId, UpdateSkillRequestDTO request) {
        SkillRequest skillRequest = findRequest(requestId);
        expireIfNeeded(skillRequest);
        requireMentor(skillRequest);
        requireStatus(skillRequest, RequestStatus.PENDING, "Only pending requests can be accepted");
        applyScheduleSuggestion(skillRequest, request);
        skillRequest.setStatus(RequestStatus.ACCEPTED);
        skillRequest.setAcceptedAt(Instant.now());
        SkillRequest saved = skillRequestRepository.save(skillRequest);
        trackStatus(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public SkillRequestResponseDTO rejectRequest(Long requestId, UpdateSkillRequestDTO request) {
        SkillRequest skillRequest = findRequest(requestId);
        expireIfNeeded(skillRequest);
        requireMentor(skillRequest);
        requireStatus(skillRequest, RequestStatus.PENDING, "Only pending requests can be rejected");
        if (request != null && request.message() != null) {
            skillRequest.setMessage(normalizeNullable(request.message()));
        }
        if (request != null) {
            skillRequest.setMentorScheduleSuggestion(normalizeNullable(request.mentorScheduleSuggestion()));
        }
        skillRequest.setStatus(RequestStatus.REJECTED);
        skillRequest.setRejectedAt(Instant.now());
        SkillRequest saved = skillRequestRepository.save(skillRequest);
        trackStatus(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public SkillRequestResponseDTO cancelRequest(Long requestId) {
        SkillRequest skillRequest = findRequest(requestId);
        expireIfNeeded(skillRequest);
        requireLearner(skillRequest);
        requireStatus(skillRequest, RequestStatus.PENDING, "Only pending requests can be cancelled");
        skillRequest.setStatus(RequestStatus.CANCELLED);
        skillRequest.setCancelledAt(Instant.now());
        SkillRequest saved = skillRequestRepository.save(skillRequest);
        trackStatus(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public SkillRequestResponseDTO completeRequest(Long requestId) {
        SkillRequest skillRequest = findRequest(requestId);
        requireMentor(skillRequest);
        requireStatus(skillRequest, RequestStatus.ACCEPTED, "Only accepted requests can be completed");
        skillRequest.setStatus(RequestStatus.COMPLETED);
        skillRequest.setCompletedAt(Instant.now());
        SkillRequest saved = skillRequestRepository.save(skillRequest);
        trackStatus(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public SkillRequestResponseDTO getRequest(Long requestId) {
        SkillRequest skillRequest = findRequest(requestId);
        expireIfNeeded(skillRequest);
        requireParticipantOrAdmin(skillRequest);
        return toResponse(skillRequest);
    }

    @Override
    @Transactional
    public Page<SkillRequestResponseDTO> getAllRequests(
            RequestStatus status,
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection) {
        expirePendingRequests();
        Pageable pageable = pageRequest(page, size, sortBy, sortDirection);
        Page<SkillRequest> requests = status == null
                ? skillRequestRepository.findAll(pageable)
                : skillRequestRepository.findByStatus(status, pageable);
        return requests.map(this::toResponse);
    }

    @Override
    @Transactional
    public Page<RequestHistoryDTO> getUserRequests(Integer page, Integer size) {
        expirePendingRequests();
        return skillRequestRepository.findByRequesterId(currentUserId(), pageRequest(page, size, "createdAt", "desc"))
                .map(this::toHistory);
    }

    @Override
    @Transactional
    public Page<RequestHistoryDTO> getMentorRequests(Integer page, Integer size) {
        expirePendingRequests();
        return skillRequestRepository.findByProviderId(currentUserId(), pageRequest(page, size, "createdAt", "desc"))
                .map(this::toHistory);
    }

    @Override
    @Transactional
    public Page<RequestHistoryDTO> getPendingRequests(Integer page, Integer size) {
        expirePendingRequests();
        return skillRequestRepository.findByProviderIdAndStatus(
                        currentUserId(),
                        RequestStatus.PENDING,
                        pageRequest(page, size, "createdAt", "desc"))
                .map(this::toHistory);
    }

    @Override
    @Transactional
    public Page<RequestHistoryDTO> getCompletedRequests(Integer page, Integer size) {
        expirePendingRequests();
        return skillRequestRepository.findCompletedRequestsByUser(
                        currentUserId(),
                        RequestStatus.COMPLETED,
                        pageRequest(page, size, "completedAt", "desc"))
                .map(this::toHistory);
    }

    private void validateExchangeType(ExchangeType exchangeType, Skill offeredSkill) {
        if (exchangeType == ExchangeType.SKILL_SWAP && offeredSkill == null) {
            throw new BadRequestException("Offered skill is required for skill swap requests");
        }
    }

    private void validateMentorAccount(User mentor) {
        boolean mentorRolePresent = mentor.getRoles()
                .stream()
                .anyMatch(role -> role.getName() == RoleName.MENTOR);
        if (!mentorRolePresent) {
            throw new BadRequestException("Selected user is not a mentor");
        }
    }

    private void validateMentorCanTeach(Long mentorId, Long requestedSkillId) {
        boolean canTeach = hasSkill(mentorId, requestedSkillId, SkillType.OFFERED);
        if (!canTeach) {
            throw new BadRequestException("Mentor does not offer the requested skill");
        }
    }

    private void validateLearnerCanOffer(Long learnerId, Skill offeredSkill) {
        if (offeredSkill != null && !hasSkill(learnerId, offeredSkill.getId(), SkillType.OFFERED)) {
            throw new BadRequestException("Learner does not offer the selected exchange skill");
        }
    }

    private boolean hasSkill(Long userId, Long skillId, SkillType skillType) {
        return userSkillRepository.findByUserId(userId)
                .stream()
                .anyMatch(userSkill -> userSkill.getSkill().getId().equals(skillId)
                        && userSkill.getSkillType() == skillType);
    }

    private SkillMatchResponseDTO buildMatch(SkillRequest request) {
        boolean mentorCanTeach = hasSkill(request.getProvider().getId(), request.getSkill().getId(), SkillType.OFFERED);
        boolean learnerCanOffer = request.getOfferedSkill() != null
                && hasSkill(request.getRequester().getId(), request.getOfferedSkill().getId(), SkillType.OFFERED);
        boolean mentorWantsOffered = request.getOfferedSkill() != null
                && hasSkill(request.getProvider().getId(), request.getOfferedSkill().getId(), SkillType.WANTED);
        int score = 0;
        score += mentorCanTeach ? 50 : 0;
        score += learnerCanOffer ? 25 : 0;
        score += mentorWantsOffered ? 25 : 0;
        String summary = score >= 75 ? "Strong skill exchange match" : "Basic mentor-learner match";
        return new SkillMatchResponseDTO(mentorCanTeach, mentorWantsOffered, learnerCanOffer, score, summary);
    }

    private void applyScheduleSuggestion(SkillRequest skillRequest, UpdateSkillRequestDTO request) {
        if (request == null) {
            return;
        }
        validateSchedule(request.suggestedStartTime(), request.suggestedEndTime());
        if (request.suggestedStartTime() != null) {
            skillRequest.setRequestedStartTime(request.suggestedStartTime());
        }
        if (request.suggestedEndTime() != null) {
            skillRequest.setRequestedEndTime(request.suggestedEndTime());
        }
        skillRequest.setMentorScheduleSuggestion(normalizeNullable(request.mentorScheduleSuggestion()));
    }

    private void validateSchedule(Instant startTime, Instant endTime) {
        if (startTime == null && endTime == null) {
            return;
        }
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new BadRequestException("Request end time must be after start time");
        }
    }

    private void requireStatus(SkillRequest request, RequestStatus expected, String message) {
        if (request.getStatus() == RequestStatus.COMPLETED) {
            throw new BadRequestException("Completed requests cannot be modified");
        }
        if (request.getStatus() != expected) {
            throw new BadRequestException(message);
        }
    }

    private void expireIfNeeded(SkillRequest request) {
        if (request.getStatus() == RequestStatus.PENDING
                && request.getExpiresAt() != null
                && request.getExpiresAt().isBefore(Instant.now())) {
            request.setStatus(RequestStatus.EXPIRED);
            SkillRequest saved = skillRequestRepository.save(request);
            trackStatus(saved);
        }
    }

    private void expirePendingRequests() {
        skillRequestRepository.expirePendingRequests(RequestStatus.PENDING, RequestStatus.EXPIRED, Instant.now());
    }

    private void requireLearner(SkillRequest request) {
        if (!request.getRequester().getId().equals(currentUserId())) {
            throw new AccessDeniedException("Only the learner can perform this action");
        }
    }

    private void requireMentor(SkillRequest request) {
        if (!request.getProvider().getId().equals(currentUserId())) {
            throw new AccessDeniedException("Only the mentor can perform this action");
        }
        if (!hasAuthority("ROLE_MENTOR")) {
            throw new AccessDeniedException("Mentor role is required for this action");
        }
    }

    private void requireParticipantOrAdmin(SkillRequest request) {
        Long userId = currentUserId();
        if (request.getRequester().getId().equals(userId) || request.getProvider().getId().equals(userId) || isAdmin()) {
            return;
        }
        throw new AccessDeniedException("Request is not visible to this user");
    }

    private SkillRequest findRequest(Long requestId) {
        return skillRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill request not found"));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Skill findSkill(Long skillId) {
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));
    }

    private SkillRequestResponseDTO toResponse(SkillRequest request) {
        Skill offeredSkill = request.getOfferedSkill();
        return new SkillRequestResponseDTO(
                request.getId(),
                request.getRequester().getId(),
                request.getRequester().getFullName(),
                request.getProvider().getId(),
                request.getProvider().getFullName(),
                request.getSkill().getId(),
                request.getSkill().getName(),
                offeredSkill == null ? null : offeredSkill.getId(),
                offeredSkill == null ? null : offeredSkill.getName(),
                request.getExchangeType(),
                request.getStatus(),
                request.getMessage(),
                request.getMentorScheduleSuggestion(),
                request.getRequestedStartTime(),
                request.getRequestedEndTime(),
                request.getAcceptedAt(),
                request.getRejectedAt(),
                request.getCancelledAt(),
                request.getCompletedAt(),
                request.getExpiresAt(),
                buildMatch(request));
    }

    private RequestHistoryDTO toHistory(SkillRequest request) {
        return new RequestHistoryDTO(
                request.getId(),
                request.getRequester().getId(),
                request.getRequester().getFullName(),
                request.getProvider().getId(),
                request.getProvider().getFullName(),
                request.getSkill().getName(),
                request.getOfferedSkill() == null ? null : request.getOfferedSkill().getName(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                request.getCompletedAt(),
                request.getExpiresAt());
    }

    private Pageable pageRequest(Integer page, Integer size, String sortBy, String sortDirection) {
        int resolvedPage = page == null ? DEFAULT_PAGE : page;
        int resolvedSize = size == null ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        String requestedSort = normalizeNullable(sortBy);
        String sortProperty = SORT_FIELDS.getOrDefault(requestedSort, "createdAt");
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(resolvedPage, resolvedSize, Sort.by(direction, sortProperty));
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            throw new AccessDeniedException("Authenticated user is required");
        }
        return principal.getId();
    }

    private boolean isAdmin() {
        return hasAuthority("ROLE_ADMIN");
    }

    private boolean hasAuthority(String authorityName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authorityName.equals(authority.getAuthority()));
    }

    private void trackStatus(SkillRequest request) {
        redisTemplate.opsForValue().set(STATUS_CACHE_PREFIX + request.getId(), request.getStatus().name(), REQUEST_TTL);
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
