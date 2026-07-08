package com.skillswap.dto.response.booking;

import java.time.Instant;

import com.skillswap.domain.entity.Availability.AvailabilityMode;

/**
 * Response DTO for a concrete available booking slot.
 */
public record TimeSlotResponse(
        Instant startTime,
        Instant endTime,
        AvailabilityMode mode,
        boolean available) {
}
