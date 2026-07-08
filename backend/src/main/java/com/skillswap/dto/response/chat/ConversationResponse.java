package com.skillswap.dto.response.chat;

import java.time.Instant;

/**
 * Response DTO for a one-to-one conversation.
 */
public record ConversationResponse(
        Long id,
        Long participantOneId,
        String participantOneName,
        Long participantTwoId,
        String participantTwoName,
        MessageResponse lastMessage,
        long unreadCount,
        Instant createdAt,
        Instant updatedAt) {
}
