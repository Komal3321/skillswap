package com.skillswap.dto.response.exchange;

import java.time.Instant;

import com.skillswap.domain.enums.ExchangeType;
import com.skillswap.domain.enums.RequestStatus;

/**
 * Response DTO for a skill exchange request.
 */
public record SkillRequestResponseDTO(
        Long id,
        Long learnerId,
        String learnerName,
        Long mentorId,
        String mentorName,
        Long requestedSkillId,
        String requestedSkillName,
        Long offeredSkillId,
        String offeredSkillName,
        ExchangeType exchangeType,
        RequestStatus status,
        String message,
        String mentorScheduleSuggestion,
        Instant requestedStartTime,
        Instant requestedEndTime,
        Instant acceptedAt,
        Instant rejectedAt,
        Instant cancelledAt,
        Instant completedAt,
        Instant expiresAt,
        SkillMatchResponseDTO match) {
}
