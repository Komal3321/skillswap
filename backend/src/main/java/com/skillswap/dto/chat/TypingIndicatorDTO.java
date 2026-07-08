package com.skillswap.dto.chat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * WebSocket DTO for typing indicators.
 */
public record TypingIndicatorDTO(
        @NotNull @Positive Long conversationId,
        @NotNull @Positive Long senderId,
        @NotNull @Positive Long receiverId,
        boolean typing) {
}
