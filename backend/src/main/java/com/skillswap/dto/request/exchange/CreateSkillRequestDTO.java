package com.skillswap.dto.request.exchange;

import java.time.Instant;

import com.skillswap.domain.enums.ExchangeType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating a skill exchange request.
 */
public record CreateSkillRequestDTO(
        @NotNull @Positive Long mentorId,
        @NotNull @Positive Long requestedSkillId,
        @Positive Long offeredSkillId,
        @NotNull ExchangeType exchangeType,
        @Size(max = 1000) String message,
        @Future Instant requestedStartTime,
        @Future Instant requestedEndTime) {
}
