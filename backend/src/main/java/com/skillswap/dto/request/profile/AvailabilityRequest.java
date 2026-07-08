package com.skillswap.dto.request.profile;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.skillswap.domain.entity.Availability.AvailabilityMode;
import com.skillswap.validation.ValidAvailabilitySlot;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for a weekly availability time slot.
 */
@ValidAvailabilitySlot
public record AvailabilityRequest(
        @NotNull DayOfWeek dayOfWeek,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @NotNull AvailabilityMode mode,
        Boolean active) {
}
