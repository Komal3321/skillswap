package com.skillswap.dto.response.profile;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.skillswap.domain.entity.Availability.AvailabilityMode;

/**
 * Response DTO for a weekly availability slot.
 */
public record AvailabilityResponse(
        Long id,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        AvailabilityMode mode,
        boolean active) {
}
