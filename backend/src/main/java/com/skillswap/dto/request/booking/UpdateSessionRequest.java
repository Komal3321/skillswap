package com.skillswap.dto.request.booking;

import java.time.Instant;

import com.skillswap.domain.enums.SessionType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

/**
 * Request body for updating or rescheduling a session.
 */
public record UpdateSessionRequest(
        @Size(max = 150) String title,
        @Size(max = 1000) String description,
        @Future Instant startTime,
        @Future Instant endTime,
        SessionType meetingType,
        @Size(max = 500) String meetingLink,
        @Size(max = 255) String location,
        @Size(max = 1000) String notes) {
}
