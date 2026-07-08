package com.skillswap.service.booking;

import java.time.LocalDate;
import java.util.List;

import com.skillswap.domain.enums.SessionType;
import com.skillswap.dto.request.booking.AvailabilityRequest;
import com.skillswap.dto.response.booking.AvailabilityResponse;
import com.skillswap.dto.response.booking.TimeSlotResponse;

/**
 * Application service for mentor availability and time slot use cases.
 */
public interface AvailabilityService {

    /**
     * Adds one availability slot for the authenticated mentor.
     *
     * @param request availability request
     * @return saved availability
     */
    AvailabilityResponse addAvailability(AvailabilityRequest request);

    /**
     * Replaces availability for the authenticated mentor.
     *
     * @param requests availability requests
     * @return saved availability
     */
    List<AvailabilityResponse> saveAvailability(List<AvailabilityRequest> requests);

    /**
     * Gets availability for the authenticated mentor.
     *
     * @return availability list
     */
    List<AvailabilityResponse> getCurrentUserAvailability();

    /**
     * Gets availability for a mentor.
     *
     * @param mentorId mentor id
     * @return availability list
     */
    List<AvailabilityResponse> getMentorAvailability(Long mentorId);

    /**
     * Gets concrete available slots for a mentor on a date.
     *
     * @param mentorId mentor id
     * @param date date to inspect
     * @param durationMinutes slot duration
     * @param meetingType desired meeting type
     * @return available slots
     */
    List<TimeSlotResponse> getAvailableSlots(
            Long mentorId,
            LocalDate date,
            Integer durationMinutes,
            SessionType meetingType);
}
