package com.skillswap.dto.response.booking;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.skillswap.domain.entity.Availability.AvailabilityMode;

/**
 * Response DTO for mentor availability.
 */
public record AvailabilityResponse(
        Long id,
        Long mentorId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        AvailabilityMode mode,
        boolean active) {
}
