package com.skillswap.dto.response.booking;

import java.time.Instant;

import com.skillswap.domain.enums.SessionStatus;
import com.skillswap.domain.enums.SessionType;

/**
 * Response DTO for a booked session.
 */
public record SessionResponse(
        Long id,
        Long skillRequestId,
        Long mentorId,
        String mentorName,
        Long learnerId,
        String learnerName,
        String title,
        String description,
        Instant startTime,
        Instant endTime,
        Integer durationMinutes,
        SessionType meetingType,
        String meetingLink,
        String location,
        SessionStatus status,
        String notes) {
}
