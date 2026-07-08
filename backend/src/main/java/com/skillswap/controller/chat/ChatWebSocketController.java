package com.skillswap.controller.chat;

import com.skillswap.dto.chat.ReadReceiptDTO;
import com.skillswap.dto.chat.TypingIndicatorDTO;
import com.skillswap.dto.request.chat.CreateMessageRequest;
import com.skillswap.dto.response.chat.MessageResponse;
import com.skillswap.service.chat.ChatService;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

/**
 * STOMP controller for real-time chat events.
 */
@Validated
@Controller
public class ChatWebSocketController {

    private final ChatService chatService;

    public ChatWebSocketController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Handles real-time message sends.
     *
     * @param request message request
     * @return persisted message
     */
    @MessageMapping("/chat.send")
    public MessageResponse sendMessage(@Valid @Payload CreateMessageRequest request) {
        return chatService.sendMessage(request);
    }

    /**
     * Handles typing indicator events.
     *
     * @param indicator typing indicator
     */
    @MessageMapping("/chat.typing")
    public void typing(@Valid @Payload TypingIndicatorDTO indicator) {
        chatService.publishTyping(indicator);
    }

    /**
     * Handles read receipt events.
     *
     * @param receipt read receipt
     */
    @MessageMapping("/chat.read")
    public void read(@Valid @Payload ReadReceiptDTO receipt) {
        chatService.markAsRead(receipt.conversationId());
    }
}
