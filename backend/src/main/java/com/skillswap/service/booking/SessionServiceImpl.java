package com.skillswap.service.booking;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;

import com.skillswap.common.exception.BadRequestException;
import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.domain.entity.Availability;
import com.skillswap.domain.entity.Session;
import com.skillswap.domain.entity.SkillRequest;
import com.skillswap.domain.enums.RequestStatus;
import com.skillswap.domain.enums.SessionStatus;
import com.skillswap.domain.enums.SessionType;
import com.skillswap.dto.request.booking.CreateSessionRequest;
import com.skillswap.dto.request.booking.UpdateSessionRequest;
import com.skillswap.dto.response.booking.SessionHistoryResponse;
import com.skillswap.dto.response.booking.SessionResponse;
import com.skillswap.repository.AvailabilityRepository;
import com.skillswap.repository.SessionRepository;
import com.skillswap.repository.SkillRequestRepository;
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
 * Default booking and session lifecycle service implementation.
 */
@Service
public class SessionServiceImpl implements SessionService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final int MIN_DURATION_MINUTES = 15;
    private static final int MAX_DURATION_MINUTES = 480;
    private static final Duration REMINDER_LEAD_TIME = Duration.ofMinutes(15);
    private static final String REMINDER_KEY_PREFIX = "session:reminder:";
    private static final Set<SessionStatus> ACTIVE_STATUSES = Set.of(
            SessionStatus.PENDING,
            SessionStatus.ACCEPTED,
            SessionStatus.RESCHEDULED);
    private static final Set<SessionStatus> TERMINAL_STATUSES = Set.of(
            SessionStatus.COMPLETED,
            SessionStatus.CANCELLED,
            SessionStatus.REJECTED,
            SessionStatus.NO_SHOW);
    private static final Map<String, String> SORT_FIELDS = Map.of(
            "id", "id",
            "startTime", "startTime",
            "createdAt", "createdAt",
            "status", "status");

    private final SessionRepository sessionRepository;
    private final SkillRequestRepository skillRequestRepository;
    private final AvailabilityRepository availabilityRepository;
    private final StringRedisTemplate redisTemplate;

    public SessionServiceImpl(
            SessionRepository sessionRepository,
            SkillRequestRepository skillRequestRepository,
            AvailabilityRepository availabilityRepository,
            StringRedisTemplate redisTemplate) {
        this.sessionRepository = sessionRepository;
        this.skillRequestRepository = skillRequestRepository;
        this.availabilityRepository = availabilityRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional
    public SessionResponse createSession(CreateSessionRequest request) {
        SkillRequest skillRequest = skillRequestRepository.findById(request.skillRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill request not found"));
        if (skillRequest.getStatus() != RequestStatus.ACCEPTED) {
            throw new BadRequestException("A booking can only be created after the skill request is accepted");
        }
        if (!skillRequest.getRequester().getId().equals(currentUserId())) {
            throw new AccessDeniedException("Only the learner can create a booking");
        }

        validateSchedule(request.startTime(), request.endTime());
        validateMeetingDetails(request.meetingType(), request.meetingLink(), request.location());
        validateMentorAvailability(
                skillRequest.getProvider().getId(),
                request.startTime(),
                request.endTime(),
                request.meetingType());
        validateNoOverlap(
                skillRequest.getProvider().getId(),
                skillRequest.getRequester().getId(),
                request.startTime(),
                request.endTime(),
                null);

        Session session = Session.builder()
                .skillRequest(skillRequest)
                .mentor(skillRequest.getProvider())
                .learner(skillRequest.getRequester())
                .title(request.title().trim())
                .description(normalizeNullable(request.description()))
                .startTime(request.startTime())
                .endTime(request.endTime())
                .durationMinutes(durationMinutes(request.startTime(), request.endTime()))
                .meetingType(request.meetingType())
                .meetingLink(resolveMeetingLink(request.meetingType(), request.meetingLink()))
                .location(normalizeNullable(request.location()))
                .status(SessionStatus.PENDING)
                .notes(normalizeNullable(request.notes()))
                .build();
        Session saved = sessionRepository.save(session);
        scheduleReminder(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public SessionResponse updateSession(Long id, UpdateSessionRequest request) {
        Session session = findSession(id);
        requireParticipantOrAdmin(session);
        requireMutable(session);
        if (changesScheduleOrMeeting(request)) {
            requireMentor(session);
        }
        applyEditableFields(session, request, false);
        Session saved = sessionRepository.save(session);
        scheduleReminder(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public SessionResponse acceptSession(Long id) {
        Session session = findSession(id);
        requireMentor(session);
        requireMutable(session);
        if (session.getStatus() != SessionStatus.PENDING && session.getStatus() != SessionStatus.RESCHEDULED) {
            throw new BadRequestException("Only pending or rescheduled sessions can be accepted");
        }
        session.setStatus(SessionStatus.ACCEPTED);
        Session saved = sessionRepository.save(session);
        scheduleReminder(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public SessionResponse rejectSession(Long id) {
        Session session = findSession(id);
        requireMentor(session);
        requireMutable(session);
        if (session.getStatus() != SessionStatus.PENDING && session.getStatus() != SessionStatus.RESCHEDULED) {
            throw new BadRequestException("Only pending or rescheduled sessions can be rejected");
        }
        session.setStatus(SessionStatus.REJECTED);
        return toResponse(sessionRepository.save(session));
    }

    @Override
    @Transactional
    public SessionResponse cancelSession(Long id) {
        Session session = findSession(id);
        requireParticipantOrAdmin(session);
        requireMutable(session);
        session.setStatus(SessionStatus.CANCELLED);
        return toResponse(sessionRepository.save(session));
    }

    @Override
    @Transactional
    public SessionResponse rescheduleSession(Long id, UpdateSessionRequest request) {
        Session session = findSession(id);
        requireMentor(session);
        requireMutable(session);
        if (request.startTime() == null || request.endTime() == null) {
            throw new BadRequestException("Start time and end time are required for rescheduling");
        }
        applyEditableFields(session, request, true);
        session.setStatus(SessionStatus.RESCHEDULED);
        Session saved = sessionRepository.save(session);
        scheduleReminder(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public SessionResponse completeSession(Long id) {
        Session session = findSession(id);
        requireMentor(session);
        if (session.getStatus() != SessionStatus.ACCEPTED) {
            throw new BadRequestException("Only accepted sessions can be completed");
        }
        session.setStatus(SessionStatus.COMPLETED);
        return toResponse(sessionRepository.save(session));
    }

    @Override
    @Transactional(readOnly = true)
    public SessionResponse getSession(Long id) {
        Session session = findSession(id);
        requireParticipantOrAdmin(session);
        return toResponse(session);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SessionResponse> getSessions(Integer page, Integer size) {
        Pageable pageable = pageRequest(page, size, "startTime", "desc");
        if (isAdmin()) {
            return sessionRepository.findAll(pageable).map(this::toResponse);
        }
        Long userId = currentUserId();
        return sessionRepository.findByMentorIdOrLearnerId(userId, userId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SessionHistoryResponse> getUpcomingSessions(Integer page, Integer size) {
        return sessionRepository.findUpcomingByUser(
                        currentUserId(),
                        Instant.now(),
                        ACTIVE_STATUSES,
                        pageRequest(page, size, "startTime", "asc"))
                .map(this::toHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SessionHistoryResponse> getSessionHistory(Integer page, Integer size) {
        return sessionRepository.findHistoryByUser(
                        currentUserId(),
                        Instant.now(),
                        TERMINAL_STATUSES,
                        pageRequest(page, size, "startTime", "desc"))
                .map(this::toHistory);
    }

    private void applyEditableFields(Session session, UpdateSessionRequest request, boolean requireScheduleValidation) {
        if (request.title() != null && !request.title().isBlank()) {
            session.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            session.setDescription(normalizeNullable(request.description()));
        }
        Instant startTime = request.startTime() == null ? session.getStartTime() : request.startTime();
        Instant endTime = request.endTime() == null ? session.getEndTime() : request.endTime();
        SessionType meetingType = request.meetingType() == null ? session.getMeetingType() : request.meetingType();
        if (request.startTime() != null || request.endTime() != null || requireScheduleValidation) {
            validateSchedule(startTime, endTime);
            validateMentorAvailability(session.getMentor().getId(), startTime, endTime, meetingType);
            validateNoOverlap(session.getMentor().getId(), session.getLearner().getId(), startTime, endTime, session.getId());
            session.setStartTime(startTime);
            session.setEndTime(endTime);
            session.setDurationMinutes(durationMinutes(startTime, endTime));
        }
        if (request.meetingType() != null) {
            session.setMeetingType(request.meetingType());
        }
        if (request.meetingLink() != null || request.meetingType() != null) {
            session.setMeetingLink(resolveMeetingLink(session.getMeetingType(), request.meetingLink()));
        }
        if (request.location() != null) {
            session.setLocation(normalizeNullable(request.location()));
        }
        if (request.notes() != null) {
            session.setNotes(normalizeNullable(request.notes()));
        }
        validateMeetingDetails(session.getMeetingType(), session.getMeetingLink(), session.getLocation());
    }

    private boolean changesScheduleOrMeeting(UpdateSessionRequest request) {
        return request.startTime() != null
                || request.endTime() != null
                || request.meetingType() != null
                || request.meetingLink() != null
                || request.location() != null;
    }

    private void validateSchedule(Instant startTime, Instant endTime) {
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new BadRequestException("Session end time must be after start time");
        }
        if (!startTime.isAfter(Instant.now())) {
            throw new BadRequestException("Session cannot be booked in the past");
        }
        int duration = durationMinutes(startTime, endTime);
        if (duration < MIN_DURATION_MINUTES || duration > MAX_DURATION_MINUTES) {
            throw new BadRequestException("Session duration must be between 15 and 480 minutes");
        }
    }

    private void validateMentorAvailability(Long mentorId, Instant startTime, Instant endTime, SessionType meetingType) {
        LocalDateTime start = LocalDateTime.ofInstant(startTime, ZoneOffset.UTC);
        LocalDateTime end = LocalDateTime.ofInstant(endTime, ZoneOffset.UTC);
        if (!start.toLocalDate().equals(end.toLocalDate())) {
            throw new BadRequestException("Session must start and end on the same UTC date");
        }
        LocalTime startLocalTime = start.toLocalTime();
        LocalTime endLocalTime = end.toLocalTime();
        boolean available = availabilityRepository
                .findByUserIdAndDayOfWeekAndActiveTrueOrderByStartTimeAsc(mentorId, start.getDayOfWeek())
                .stream()
                .filter(availability -> availabilitySupportsType(availability, meetingType))
                .anyMatch(availability -> !startLocalTime.isBefore(availability.getStartTime())
                        && !endLocalTime.isAfter(availability.getEndTime()));
        if (!available) {
            throw new BadRequestException("Requested slot is outside mentor availability");
        }
    }

    private void validateNoOverlap(
            Long mentorId,
            Long learnerId,
            Instant startTime,
            Instant endTime,
            Long excludedSessionId) {
        if (sessionRepository.existsOverlappingSession(
                mentorId,
                learnerId,
                startTime,
                endTime,
                ACTIVE_STATUSES,
                excludedSessionId)) {
            throw new BadRequestException("Session overlaps an existing booking");
        }
    }

    private boolean availabilitySupportsType(Availability availability, SessionType meetingType) {
        return availability.getMode().name().equals(meetingType.name()) || "BOTH".equals(availability.getMode().name());
    }

    private void validateMeetingDetails(SessionType meetingType, String meetingLink, String location) {
        if (meetingType == SessionType.OFFLINE && (location == null || location.isBlank())) {
            throw new BadRequestException("Location is required for offline sessions");
        }
        if (meetingType == SessionType.ONLINE && meetingLink != null && meetingLink.length() > 500) {
            throw new BadRequestException("Meeting link must be 500 characters or fewer");
        }
    }

    private String resolveMeetingLink(SessionType meetingType, String meetingLink) {
        String normalized = normalizeNullable(meetingLink);
        if (meetingType == SessionType.ONLINE && normalized == null) {
            return "https://meet.jit.si/skillswap-" + java.util.UUID.randomUUID();
        }
        return normalized;
    }

    private void requireMutable(Session session) {
        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new BadRequestException("Completed sessions are read-only");
        }
        if (session.getEndTime().isBefore(Instant.now())) {
            throw new BadRequestException("Past sessions cannot be modified");
        }
    }

    private void requireMentor(Session session) {
        if (!session.getMentor().getId().equals(currentUserId()) || !hasAuthority("ROLE_MENTOR")) {
            throw new AccessDeniedException("Only the mentor can perform this action");
        }
    }

    private void requireParticipantOrAdmin(Session session) {
        Long userId = currentUserId();
        if (isAdmin() || session.getMentor().getId().equals(userId) || session.getLearner().getId().equals(userId)) {
            return;
        }
        throw new AccessDeniedException("Session is not visible to this user");
    }

    private Session findSession(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
    }

    private SessionResponse toResponse(Session session) {
        return new SessionResponse(
                session.getId(),
                session.getSkillRequest().getId(),
                session.getMentor().getId(),
                session.getMentor().getFullName(),
                session.getLearner().getId(),
                session.getLearner().getFullName(),
                session.getTitle(),
                session.getDescription(),
                session.getStartTime(),
                session.getEndTime(),
                session.getDurationMinutes(),
                session.getMeetingType(),
                session.getMeetingLink(),
                session.getLocation(),
                session.getStatus(),
                session.getNotes());
    }

    private SessionHistoryResponse toHistory(Session session) {
        return new SessionHistoryResponse(
                session.getId(),
                session.getTitle(),
                session.getMentor().getId(),
                session.getMentor().getFullName(),
                session.getLearner().getId(),
                session.getLearner().getFullName(),
                session.getStartTime(),
                session.getEndTime(),
                session.getDurationMinutes(),
                session.getMeetingType(),
                session.getStatus());
    }

    private Pageable pageRequest(Integer page, Integer size, String sortBy, String sortDirection) {
        int resolvedPage = page == null ? DEFAULT_PAGE : page;
        int resolvedSize = size == null ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        String sortProperty = SORT_FIELDS.getOrDefault(sortBy, "startTime");
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(resolvedPage, resolvedSize, Sort.by(direction, sortProperty));
    }

    private int durationMinutes(Instant startTime, Instant endTime) {
        return Math.toIntExact(Duration.between(startTime, endTime).toMinutes());
    }

    private void scheduleReminder(Session session) {
        Instant reminderTime = session.getStartTime().minus(REMINDER_LEAD_TIME);
        Duration ttl = Duration.between(Instant.now(), reminderTime);
        if (ttl.isPositive()) {
            redisTemplate.opsForValue().set(REMINDER_KEY_PREFIX + session.getId(), session.getStatus().name(), ttl);
        }
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

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
