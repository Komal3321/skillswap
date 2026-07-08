package com.skillswap.controller.chat;

import com.skillswap.common.api.ApiResponse;
import com.skillswap.dto.request.chat.CreateMessageRequest;
import com.skillswap.dto.response.chat.ConversationResponse;
import com.skillswap.dto.response.chat.MessageResponse;
import com.skillswap.service.chat.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for chat APIs.
 */
@Validated
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Lists recent conversations for the authenticated user.
     *
     * @param page page number
     * @param size page size
     * @return conversations
     */
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<Page<ConversationResponse>>> getConversations(
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size) {
        return ResponseEntity.ok(ApiResponse.success("Conversations fetched successfully",
                chatService.getConversations(page, size)));
    }

    /**
     * Gets one conversation.
     *
     * @param id conversation id
     * @return conversation
     */
    @GetMapping("/conversations/{id}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversation(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Conversation fetched successfully",
                chatService.getConversation(id)));
    }

    /**
     * Gets conversation history.
     *
     * @param conversationId conversation id
     * @param page page number
     * @param size page size
     * @return messages
     */
    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size) {
        return ResponseEntity.ok(ApiResponse.success("Messages fetched successfully",
                chatService.getConversationHistory(conversationId, page, size)));
    }

    /**
     * Sends a chat message.
     *
     * @param request message request
     * @return saved message
     */
    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @Valid @RequestBody CreateMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message sent successfully", chatService.sendMessage(request)));
    }

    /**
     * Edits a chat message.
     *
     * @param id message id
     * @param request edit request
     * @return edited message
     */
    @PutMapping("/messages/{id}")
    public ResponseEntity<ApiResponse<MessageResponse>> editMessage(
            @PathVariable Long id,
            @Valid @RequestBody CreateMessageRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Message updated successfully",
                chatService.editMessage(id, request)));
    }

    /**
     * Soft deletes a chat message.
     *
     * @param id message id
     * @return empty success response
     */
    @DeleteMapping("/messages/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable Long id) {
        chatService.deleteMessage(id);
        return ResponseEntity.ok(ApiResponse.success("Message deleted successfully"));
    }

    /**
     * Gets total unread message count.
     *
     * @return unread count
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        return ResponseEntity.ok(ApiResponse.success("Unread count fetched successfully",
                chatService.getUnreadCount()));
    }

    /**
     * Searches messages in one conversation.
     *
     * @param conversationId conversation id
     * @param query search query
     * @param page page number
     * @param size page size
     * @return matching messages
     */
    @GetMapping("/messages/{conversationId}/search")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> searchMessages(
            @PathVariable Long conversationId,
            @RequestParam @Size(max = 120) String query,
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size) {
        return ResponseEntity.ok(ApiResponse.success("Messages searched successfully",
                chatService.searchMessages(conversationId, query, page, size)));
    }
}
