package com.skillswap.service.booking;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.skillswap.common.exception.BadRequestException;
import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.domain.entity.Availability;
import com.skillswap.domain.entity.Availability.AvailabilityMode;
import com.skillswap.domain.entity.Role.RoleName;
import com.skillswap.domain.entity.User;
import com.skillswap.domain.enums.SessionStatus;
import com.skillswap.domain.enums.SessionType;
import com.skillswap.dto.request.booking.AvailabilityRequest;
import com.skillswap.dto.response.booking.AvailabilityResponse;
import com.skillswap.dto.response.booking.TimeSlotResponse;
import com.skillswap.repository.AvailabilityRepository;
import com.skillswap.repository.SessionRepository;
import com.skillswap.repository.UserRepository;
import com.skillswap.security.user.CustomUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default mentor availability service implementation.
 */
@Service
public class AvailabilityServiceImpl implements AvailabilityService {

    private static final int DEFAULT_SLOT_DURATION_MINUTES = 60;
    private static final int MIN_SLOT_DURATION_MINUTES = 15;
    private static final int MAX_SLOT_DURATION_MINUTES = 480;
    private static final Set<SessionStatus> ACTIVE_STATUSES = Set.of(
            SessionStatus.PENDING,
            SessionStatus.ACCEPTED,
            SessionStatus.RESCHEDULED);

    private final AvailabilityRepository availabilityRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public AvailabilityServiceImpl(
            AvailabilityRepository availabilityRepository,
            SessionRepository sessionRepository,
            UserRepository userRepository) {
        this.availabilityRepository = availabilityRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public AvailabilityResponse addAvailability(AvailabilityRequest request) {
        User mentor = currentMentor();
        Availability availability = Availability.builder()
                .user(mentor)
                .dayOfWeek(request.dayOfWeek())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .mode(request.mode())
                .active(request.active() == null || request.active())
                .build();
        return toResponse(availabilityRepository.save(availability));
    }

    @Override
    @Transactional
    public List<AvailabilityResponse> saveAvailability(List<AvailabilityRequest> requests) {
        User mentor = currentMentor();
        List<AvailabilityRequest> slots = requests == null ? List.of() : requests;
        validateNoOverlaps(slots);
        availabilityRepository.deleteByUserId(mentor.getId());
        return slots.stream()
                .map(request -> Availability.builder()
                        .user(mentor)
                        .dayOfWeek(request.dayOfWeek())
                        .startTime(request.startTime())
                        .endTime(request.endTime())
                        .mode(request.mode())
                        .active(request.active() == null || request.active())
                        .build())
                .map(availabilityRepository::save)
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getCurrentUserAvailability() {
        return getMentorAvailability(currentUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getMentorAvailability(Long mentorId) {
        ensureUserExists(mentorId);
        return availabilityRepository.findByUserIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(mentorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getAvailableSlots(
            Long mentorId,
            LocalDate date,
            Integer durationMinutes,
            SessionType meetingType) {
        ensureUserExists(mentorId);
        LocalDate targetDate = date == null ? LocalDate.now(ZoneOffset.UTC) : date;
        int duration = durationMinutes == null ? DEFAULT_SLOT_DURATION_MINUTES : durationMinutes;
        if (duration < MIN_SLOT_DURATION_MINUTES || duration > MAX_SLOT_DURATION_MINUTES) {
            throw new BadRequestException("Slot duration must be between 15 and 480 minutes");
        }
        SessionType requestedType = meetingType == null ? SessionType.ONLINE : meetingType;
        return availabilityRepository
                .findByUserIdAndDayOfWeekAndActiveTrueOrderByStartTimeAsc(mentorId, targetDate.getDayOfWeek())
                .stream()
                .filter(availability -> supportsType(availability.getMode(), requestedType))
                .flatMap(availability -> buildSlots(mentorId, targetDate, availability, duration).stream())
                .toList();
    }

    private List<TimeSlotResponse> buildSlots(
            Long mentorId,
            LocalDate date,
            Availability availability,
            int durationMinutes) {
        LocalDateTime cursor = LocalDateTime.of(date, availability.getStartTime());
        LocalDateTime availabilityEnd = LocalDateTime.of(date, availability.getEndTime());
        java.util.ArrayList<TimeSlotResponse> slots = new java.util.ArrayList<>();
        while (!cursor.plusMinutes(durationMinutes).isAfter(availabilityEnd)) {
            LocalDateTime slotEnd = cursor.plusMinutes(durationMinutes);
            java.time.Instant startInstant = cursor.toInstant(ZoneOffset.UTC);
            java.time.Instant endInstant = slotEnd.toInstant(ZoneOffset.UTC);
            boolean available = !startInstant.isBefore(java.time.Instant.now())
                    && !sessionRepository.existsOverlappingSession(
                            mentorId,
                            mentorId,
                            startInstant,
                            endInstant,
                            ACTIVE_STATUSES,
                            null);
            slots.add(new TimeSlotResponse(startInstant, endInstant, availability.getMode(), available));
            cursor = cursor.plusMinutes(durationMinutes);
        }
        return slots;
    }

    private void validateNoOverlaps(List<AvailabilityRequest> requests) {
        List<AvailabilityRequest> activeSlots = requests.stream()
                .filter(request -> request.active() == null || request.active())
                .sorted(Comparator.comparing(AvailabilityRequest::dayOfWeek)
                        .thenComparing(AvailabilityRequest::startTime))
                .toList();
        Set<String> exactSlots = new HashSet<>();
        for (AvailabilityRequest slot : activeSlots) {
            String key = slot.dayOfWeek() + "|" + slot.startTime() + "|" + slot.endTime();
            if (!exactSlots.add(key)) {
                throw new BadRequestException("Duplicate availability slot");
            }
            activeSlots.stream()
                    .filter(other -> other != slot && other.dayOfWeek() == slot.dayOfWeek())
                    .filter(other -> slot.startTime().isBefore(other.endTime())
                            && slot.endTime().isAfter(other.startTime()))
                    .findFirst()
                    .ifPresent(other -> {
                        throw new BadRequestException("Availability slots cannot overlap on " + slot.dayOfWeek());
                    });
        }
    }

    private boolean supportsType(AvailabilityMode availabilityMode, SessionType meetingType) {
        return availabilityMode.name().equals(meetingType.name()) || availabilityMode == AvailabilityMode.BOTH;
    }

    private User currentMentor() {
        User user = ensureUserExists(currentUserId());
        boolean mentorRolePresent = user.getRoles()
                .stream()
                .anyMatch(role -> role.getName() == RoleName.MENTOR);
        if (!mentorRolePresent) {
            throw new AccessDeniedException("Mentor role is required to manage availability");
        }
        return user;
    }

    private User ensureUserExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private AvailabilityResponse toResponse(Availability availability) {
        return new AvailabilityResponse(
                availability.getId(),
                availability.getUser().getId(),
                availability.getDayOfWeek(),
                availability.getStartTime(),
                availability.getEndTime(),
                availability.getMode(),
                availability.isActive());
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            throw new AccessDeniedException("Authenticated user is required");
        }
        return principal.getId();
    }
}
