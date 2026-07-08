package com.skillswap.repository;

import java.util.Optional;

import com.skillswap.domain.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for one-to-one chat conversations.
 */
public interface ConversationRepository extends BaseRepository<Conversation, Long> {

    /**
     * Finds a conversation between two users regardless of participant order.
     *
     * @param firstUserId first user id
     * @param secondUserId second user id
     * @return conversation when present
     */
    @Query("""
            select conversation
            from Conversation conversation
            where (conversation.participantOne.id = :firstUserId and conversation.participantTwo.id = :secondUserId)
               or (conversation.participantOne.id = :secondUserId and conversation.participantTwo.id = :firstUserId)
            """)
    Optional<Conversation> findBetweenUsers(
            @Param("firstUserId") Long firstUserId,
            @Param("secondUserId") Long secondUserId);

    /**
     * Lists recent conversations for a user.
     *
     * @param userId user id
     * @param pageable page request
     * @return recent conversations
     */
    @Query("""
            select conversation
            from Conversation conversation
            where conversation.participantOne.id = :userId or conversation.participantTwo.id = :userId
            order by conversation.updatedAt desc
            """)
    Page<Conversation> findRecentByUser(@Param("userId") Long userId, Pageable pageable);
}
