package com.skillswap.dto.request.exchange;

import java.time.Instant;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

/**
 * Request body for mentor decisions and schedule suggestions.
 */
public record UpdateSkillRequestDTO(
        @Size(max = 1000) String message,
        @Size(max = 1000) String mentorScheduleSuggestion,
        @Future Instant suggestedStartTime,
        @Future Instant suggestedEndTime) {
}
