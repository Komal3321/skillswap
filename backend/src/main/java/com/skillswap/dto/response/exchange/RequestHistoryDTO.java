package com.skillswap.dto.response.exchange;

import java.time.Instant;

import com.skillswap.domain.enums.RequestStatus;

/**
 * Compact response DTO for request history views.
 */
public record RequestHistoryDTO(
        Long id,
        Long learnerId,
        String learnerName,
        Long mentorId,
        String mentorName,
        String requestedSkillName,
        String offeredSkillName,
        RequestStatus status,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt,
        Instant expiresAt) {
}
