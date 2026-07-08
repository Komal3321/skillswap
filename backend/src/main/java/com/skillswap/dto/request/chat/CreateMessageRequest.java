package com.skillswap.dto.request.chat;

import com.skillswap.domain.enums.MessageType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request payload for sending or editing a chat message.
 */
public record CreateMessageRequest(
        @Positive Long conversationId,
        @NotNull @Positive Long receiverId,
        @Size(max = 4000) String content,
        @Size(max = 500) String attachmentUrl,
        @NotNull MessageType messageType,
        @Size(max = 500) String voiceNoteMetadata) {
}
