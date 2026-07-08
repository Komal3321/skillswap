package com.skillswap.service.chat;

import com.skillswap.dto.chat.ReadReceiptDTO;
import com.skillswap.dto.chat.TypingIndicatorDTO;
import com.skillswap.dto.request.chat.CreateMessageRequest;
import com.skillswap.dto.response.chat.ConversationResponse;
import com.skillswap.dto.response.chat.MessageResponse;
import org.springframework.data.domain.Page;

/**
 * Application service for one-to-one chat use cases.
 */
public interface ChatService {

    /**
     * Sends a message from the authenticated user.
     *
     * @param request message request
     * @return saved message
     */
    MessageResponse sendMessage(CreateMessageRequest request);

    /**
     * Edits a message within the edit window.
     *
     * @param messageId message id
     * @param request edit request
     * @return edited message
     */
    MessageResponse editMessage(Long messageId, CreateMessageRequest request);

    /**
     * Soft deletes a message.
     *
     * @param messageId message id
     */
    void deleteMessage(Long messageId);

    /**
     * Marks messages in a conversation as read for the authenticated user.
     *
     * @param conversationId conversation id
     */
    void markAsRead(Long conversationId);

    /**
     * Gets one conversation visible to the authenticated user.
     *
     * @param conversationId conversation id
     * @return conversation
     */
    ConversationResponse getConversation(Long conversationId);

    /**
     * Lists recent conversations for the authenticated user.
     *
     * @param page page number
     * @param size page size
     * @return paged conversations
     */
    Page<ConversationResponse> getConversations(Integer page, Integer size);

    /**
     * Gets conversation message history.
     *
     * @param conversationId conversation id
     * @param page page number
     * @param size page size
     * @return paged messages
     */
    Page<MessageResponse> getConversationHistory(Long conversationId, Integer page, Integer size);

    /**
     * Searches messages in a conversation.
     *
     * @param conversationId conversation id
     * @param query search text
     * @param page page number
     * @param size page size
     * @return matching messages
     */
    Page<MessageResponse> searchMessages(Long conversationId, String query, Integer page, Integer size);

    /**
     * Counts unread messages for the authenticated user.
     *
     * @return unread count
     */
    long getUnreadCount();

    /**
     * Handles typing indicator events.
     *
     * @param indicator typing indicator
     */
    void publishTyping(TypingIndicatorDTO indicator);

    /**
     * Handles read receipt events.
     *
     * @param receipt read receipt
     */
    void publishReadReceipt(ReadReceiptDTO receipt);

    /**
     * Marks a user online.
     *
     * @param userId user id
     */
    void markOnline(Long userId);

    /**
     * Marks a user offline.
     *
     * @param userId user id
     */
    void markOffline(Long userId);
}
