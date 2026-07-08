package com.skillswap.dto.chat;

import java.time.Instant;

/**
 * WebSocket DTO for online/offline user status.
 */
public record OnlineStatusDTO(
        Long userId,
        boolean online,
        Instant changedAt) {
}
