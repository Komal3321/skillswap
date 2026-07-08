package com.skillswap.service.profile;

import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.skillswap.common.exception.BadRequestException;
import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.domain.entity.Availability;
import com.skillswap.domain.entity.Skill;
import com.skillswap.domain.entity.User;
import com.skillswap.domain.entity.UserProfile;
import com.skillswap.domain.entity.UserSkill;
import com.skillswap.domain.entity.UserSkill.SkillType;
import com.skillswap.dto.request.profile.AvailabilityRequest;
import com.skillswap.dto.request.profile.UpdateProfileRequest;
import com.skillswap.dto.request.profile.UserProfileRequest;
import com.skillswap.dto.response.profile.AvailabilityResponse;
import com.skillswap.dto.response.profile.UserProfileResponse;
import com.skillswap.dto.response.profile.UserProfileResponse.UserSkillResponse;
import com.skillswap.repository.AvailabilityRepository;
import com.skillswap.repository.SkillRepository;
import com.skillswap.repository.UserProfileRepository;
import com.skillswap.repository.UserRepository;
import com.skillswap.repository.UserSkillRepository;
import com.skillswap.security.user.CustomUserDetails;
import com.skillswap.service.storage.FileStorageService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Default profile service implementation.
 */
@Service
public class UserProfileServiceImpl implements UserProfileService {

    private static final String PROFILE_PHOTO_FOLDER = "profile-photos";
    private static final String CERTIFICATE_FOLDER = "certificates";
    private static final String PORTFOLIO_FOLDER = "portfolio";

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSkillRepository userSkillRepository;
    private final AvailabilityRepository availabilityRepository;
    private final SkillRepository skillRepository;
    private final FileStorageService fileStorageService;

    public UserProfileServiceImpl(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            UserSkillRepository userSkillRepository,
            AvailabilityRepository availabilityRepository,
            SkillRepository skillRepository,
            FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userSkillRepository = userSkillRepository;
        this.availabilityRepository = availabilityRepository;
        this.skillRepository = skillRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        return getUserProfile(currentUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = findUser(userId);
        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        List<UserSkill> skills = userSkillRepository.findByUserId(userId);
        List<Availability> availability = availabilityRepository.findByUserIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(userId);
        return toProfileResponse(user, profile, skills, availability);
    }

    @Override
    @Transactional
    public UserProfileResponse updateCurrentUserProfile(UpdateProfileRequest request) {
        Long userId = currentUserId();
        User user = findUser(userId);
        UserProfile profile = findOrCreateProfile(user);
        UserProfileRequest payload = request.profile();

        user.setFullName(normalizeRequired(payload.fullName(), user.getFullName(), "Full name"));
        user.setPhoneNumber(normalizeNullable(payload.phoneNumber()));
        user.setBio(normalizeNullable(payload.bio()));

        profile.setCity(normalizeNullable(payload.city()));
        profile.setCountry(normalizeNullable(payload.country()));
        profile.setTimeZone(normalizeNullable(payload.timeZone()));
        profile.setPreferredLearningMode(payload.preferredLearningMode());
        profile.setExperience(normalizeNullable(payload.experience()));
        profile.setLanguages(normalizeStringSet(payload.languages()));
        profile.setPortfolioLinks(normalizeStringSet(payload.portfolioLinks()));

        userRepository.save(user);
        userProfileRepository.save(profile);
        replaceSkills(user, payload.skillsOffered(), SkillType.OFFERED);
        replaceSkills(user, payload.skillsWanted(), SkillType.WANTED);
        return getUserProfile(userId);
    }

    @Override
    @Transactional
    public UserProfileResponse uploadProfilePhoto(MultipartFile file) {
        Long userId = currentUserId();
        User user = findUser(userId);
        user.setProfileImageUrl(fileStorageService.store(file, PROFILE_PHOTO_FOLDER));
        userRepository.save(user);
        return getUserProfile(userId);
    }

    @Override
    @Transactional
    public UserProfileResponse uploadCertificate(MultipartFile file) {
        Long userId = currentUserId();
        User user = findUser(userId);
        UserProfile profile = findOrCreateProfile(user);
        profile.getCertificateUrls().add(fileStorageService.store(file, CERTIFICATE_FOLDER));
        userProfileRepository.save(profile);
        return getUserProfile(userId);
    }

    @Override
    @Transactional
    public UserProfileResponse uploadPortfolioDocument(MultipartFile file) {
        Long userId = currentUserId();
        User user = findUser(userId);
        UserProfile profile = findOrCreateProfile(user);
        profile.getPortfolioDocumentUrls().add(fileStorageService.store(file, PORTFOLIO_FOLDER));
        userProfileRepository.save(profile);
        return getUserProfile(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getCurrentUserAvailability() {
        return availabilityRepository.findByUserIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(currentUserId())
                .stream()
                .map(this::toAvailabilityResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<AvailabilityResponse> updateCurrentUserAvailability(List<AvailabilityRequest> requests) {
        Long userId = currentUserId();
        User user = findUser(userId);
        List<AvailabilityRequest> requestedSlots = requests == null ? List.of() : requests;
        validateNoOverlaps(requestedSlots);

        availabilityRepository.deleteByUserId(userId);
        List<Availability> saved = requestedSlots.stream()
                .map(request -> Availability.builder()
                        .user(user)
                        .dayOfWeek(request.dayOfWeek())
                        .startTime(request.startTime())
                        .endTime(request.endTime())
                        .mode(request.mode())
                        .active(request.active() == null || request.active())
                        .build())
                .map(availabilityRepository::save)
                .toList();
        return saved.stream().map(this::toAvailabilityResponse).toList();
    }

    private void replaceSkills(
            User user,
            Set<UserProfileRequest.SkillSelectionRequest> requests,
            SkillType skillType) {
        userSkillRepository.deleteByUserIdAndSkillType(user.getId(), skillType);
        if (requests == null || requests.isEmpty()) {
            return;
        }

        Set<Long> seenSkillIds = new HashSet<>();
        for (UserProfileRequest.SkillSelectionRequest request : requests) {
            if (!seenSkillIds.add(request.skillId())) {
                throw new BadRequestException("Duplicate skill id in " + skillType.name().toLowerCase() + " skills");
            }
            Skill skill = skillRepository.findById(request.skillId())
                    .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + request.skillId()));
            UserSkill userSkill = UserSkill.builder()
                    .user(user)
                    .skill(skill)
                    .skillType(skillType)
                    .proficiencyLevel(request.proficiencyLevel())
                    .yearsOfExperience(request.yearsOfExperience())
                    .description(normalizeNullable(request.description()))
                    .verified(false)
                    .build();
            userSkillRepository.save(userSkill);
        }
    }

    private void validateNoOverlaps(List<AvailabilityRequest> requests) {
        List<AvailabilityRequest> activeSlots = requests.stream()
                .filter(request -> request.active() == null || request.active())
                .sorted(Comparator.comparing(AvailabilityRequest::dayOfWeek)
                        .thenComparing(AvailabilityRequest::startTime))
                .toList();

        for (DayOfWeek day : DayOfWeek.values()) {
            List<AvailabilityRequest> slotsForDay = activeSlots.stream()
                    .filter(slot -> slot.dayOfWeek() == day)
                    .sorted(Comparator.comparing(AvailabilityRequest::startTime))
                    .toList();
            AvailabilityRequest previous = null;
            for (AvailabilityRequest current : slotsForDay) {
                if (previous != null && current.startTime().isBefore(previous.endTime())) {
                    throw new BadRequestException("Availability slots cannot overlap on " + day.name());
                }
                previous = current;
            }
        }
    }

    private UserProfile findOrCreateProfile(User user) {
        return userProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> userProfileRepository.save(UserProfile.builder()
                        .user(user)
                        .build()));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserProfileResponse toProfileResponse(
            User user,
            UserProfile profile,
            List<UserSkill> skills,
            List<Availability> availability) {
        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getBio(),
                user.getProfileImageUrl(),
                profile == null ? null : profile.getCity(),
                profile == null ? null : profile.getCountry(),
                profile == null ? null : profile.getTimeZone(),
                profile == null ? null : profile.getPreferredLearningMode(),
                profile == null ? null : profile.getExperience(),
                profile == null ? Set.of() : Set.copyOf(profile.getLanguages()),
                profile == null ? Set.of() : Set.copyOf(profile.getPortfolioLinks()),
                profile == null ? Set.of() : Set.copyOf(profile.getPortfolioDocumentUrls()),
                profile == null ? Set.of() : Set.copyOf(profile.getCertificateUrls()),
                toSkillResponses(skills, SkillType.OFFERED),
                toSkillResponses(skills, SkillType.WANTED),
                availability.stream().map(this::toAvailabilityResponse).toList());
    }

    private List<UserSkillResponse> toSkillResponses(List<UserSkill> skills, SkillType skillType) {
        return skills.stream()
                .filter(userSkill -> userSkill.getSkillType() == skillType)
                .map(userSkill -> new UserSkillResponse(
                        userSkill.getId(),
                        userSkill.getSkill().getId(),
                        userSkill.getSkill().getName(),
                        userSkill.getSkillType(),
                        userSkill.getProficiencyLevel(),
                        userSkill.getYearsOfExperience(),
                        userSkill.getDescription(),
                        userSkill.isVerified()))
                .toList();
    }

    private AvailabilityResponse toAvailabilityResponse(Availability availability) {
        return new AvailabilityResponse(
                availability.getId(),
                availability.getDayOfWeek(),
                availability.getStartTime(),
                availability.getEndTime(),
                availability.getMode(),
                availability.isActive());
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            throw new BadRequestException("Authenticated user is required");
        }
        return principal.getId();
    }

    private String normalizeRequired(String value, String fallback, String label) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            if (fallback == null || fallback.isBlank()) {
                throw new BadRequestException(label + " is required");
            }
            return fallback;
        }
        return normalized;
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Set<String> normalizeStringSet(Set<String> values) {
        if (values == null) {
            return new HashSet<>();
        }
        return values.stream()
                .map(this::normalizeNullable)
                .filter(value -> value != null)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
