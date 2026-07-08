package com.skillswap.dto.request.booking;

import java.time.Instant;

import com.skillswap.domain.enums.SessionType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating a session booking.
 */
public record CreateSessionRequest(
        @NotNull @Positive Long skillRequestId,
        @NotBlank @Size(max = 150) String title,
        @Size(max = 1000) String description,
        @NotNull @Future Instant startTime,
        @NotNull @Future Instant endTime,
        @NotNull SessionType meetingType,
        @Size(max = 500) String meetingLink,
        @Size(max = 255) String location,
        @Size(max = 1000) String notes) {
}
