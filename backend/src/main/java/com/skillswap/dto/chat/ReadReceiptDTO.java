package com.skillswap.dto.chat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * WebSocket DTO for read receipts.
 */
public record ReadReceiptDTO(
        @NotNull @Positive Long conversationId,
        @NotNull @Positive Long readerId) {
}
