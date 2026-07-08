package com.skillswap.dto.response.chat;

import java.time.Instant;

import com.skillswap.domain.enums.MessageStatus;
import com.skillswap.domain.enums.MessageType;

/**
 * Response DTO for a chat message.
 */
public record MessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        String senderName,
        Long receiverId,
        String receiverName,
        String content,
        String attachmentUrl,
        MessageType messageType,
        MessageStatus status,
        boolean edited,
        boolean deleted,
        Instant readAt,
        Instant createdAt,
        Instant updatedAt) {
}
