package com.skillswap.dto.response.booking;

import java.time.Instant;

import com.skillswap.domain.enums.SessionStatus;
import com.skillswap.domain.enums.SessionType;

/**
 * Compact response DTO for session history.
 */
public record SessionHistoryResponse(
        Long id,
        String title,
        Long mentorId,
        String mentorName,
        Long learnerId,
        String learnerName,
        Instant startTime,
        Instant endTime,
        Integer durationMinutes,
        SessionType meetingType,
        SessionStatus status) {
}
