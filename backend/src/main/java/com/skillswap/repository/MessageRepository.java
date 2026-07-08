package com.skillswap.repository;

import java.util.Optional;

import com.skillswap.domain.entity.Message;
import com.skillswap.domain.enums.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for chat messages.
 */
public interface MessageRepository extends BaseRepository<Message, Long> {

    /**
     * Lists conversation history.
     *
     * @param conversationId conversation id
     * @param pageable page request
     * @return paged messages
     */
    Page<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    /**
     * Finds a message inside a conversation.
     *
     * @param id message id
     * @param conversationId conversation id
     * @return message when present
     */
    Optional<Message> findByIdAndConversationId(Long id, Long conversationId);

    /**
     * Counts unread messages for a receiver.
     *
     * @param receiverId receiver id
     * @param status unread status
     * @return unread count
     */
    long countByReceiverIdAndStatusAndDeletedFalse(Long receiverId, MessageStatus status);

    /**
     * Counts unread messages in one conversation.
     *
     * @param conversationId conversation id
     * @param receiverId receiver id
     * @param status unread status
     * @return unread count
     */
    long countByConversationIdAndReceiverIdAndStatusAndDeletedFalse(
            Long conversationId,
            Long receiverId,
            MessageStatus status);

    /**
     * Marks unread messages as read for one receiver.
     *
     * @param conversationId conversation id
     * @param receiverId receiver id
     * @param unreadStatus unread status
     * @param readStatus read status
     * @return updated row count
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Message message
            set message.status = :readStatus,
                message.readAt = CURRENT_TIMESTAMP
            where message.conversation.id = :conversationId
              and message.receiver.id = :receiverId
              and message.status = :unreadStatus
              and message.deleted = false
            """)
    int markConversationAsRead(
            @Param("conversationId") Long conversationId,
            @Param("receiverId") Long receiverId,
            @Param("unreadStatus") MessageStatus unreadStatus,
            @Param("readStatus") MessageStatus readStatus);

    /**
     * Searches messages within a conversation.
     *
     * @param conversationId conversation id
     * @param query search text
     * @param pageable page request
     * @return matching messages
     */
    @Query("""
            select message
            from Message message
            where message.conversation.id = :conversationId
              and message.deleted = false
              and lower(message.content) like lower(concat('%', :query, '%'))
            order by message.createdAt desc
            """)
    Page<Message> searchMessages(
            @Param("conversationId") Long conversationId,
            @Param("query") String query,
            Pageable pageable);
}
