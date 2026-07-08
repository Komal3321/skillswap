package com.skillswap.dto.request.booking;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.skillswap.domain.entity.Availability.AvailabilityMode;
import com.skillswap.validation.ValidBookingAvailabilitySlot;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for mentor availability management.
 */
@ValidBookingAvailabilitySlot
public record AvailabilityRequest(
        @NotNull DayOfWeek dayOfWeek,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @NotNull AvailabilityMode mode,
        Boolean active) {
}
