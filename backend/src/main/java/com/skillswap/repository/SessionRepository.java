package com.skillswap.repository;

import java.time.Instant;
import java.util.Collection;

import com.skillswap.domain.entity.Session;
import com.skillswap.domain.enums.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for booked learning sessions.
 */
public interface SessionRepository extends BaseRepository<Session, Long> {

    /**
     * Finds sessions for a mentor.
     *
     * @param mentorId mentor id
     * @param pageable page request
     * @return mentor sessions
     */
    Page<Session> findByMentorId(Long mentorId, Pageable pageable);

    /**
     * Finds sessions for a learner.
     *
     * @param learnerId learner id
     * @param pageable page request
     * @return learner sessions
     */
    Page<Session> findByLearnerId(Long learnerId, Pageable pageable);

    /**
     * Finds sessions involving a user.
     *
     * @param mentorId mentor id
     * @param learnerId learner id
     * @param pageable page request
     * @return user sessions
     */
    Page<Session> findByMentorIdOrLearnerId(Long mentorId, Long learnerId, Pageable pageable);

    /**
     * Finds upcoming sessions for a user.
     *
     * @param userId user id
     * @param now current instant
     * @param statuses active statuses
     * @param pageable page request
     * @return upcoming sessions
     */
    @Query("""
            select session
            from Session session
            where (session.mentor.id = :userId or session.learner.id = :userId)
              and session.startTime >= :now
              and session.status in :statuses
            """)
    Page<Session> findUpcomingByUser(
            @Param("userId") Long userId,
            @Param("now") Instant now,
            @Param("statuses") Collection<SessionStatus> statuses,
            Pageable pageable);

    /**
     * Finds completed sessions for a user.
     *
     * @param userId user id
     * @param status completed status
     * @param pageable page request
     * @return completed sessions
     */
    @Query("""
            select session
            from Session session
            where (session.mentor.id = :userId or session.learner.id = :userId)
              and session.status = :status
            """)
    Page<Session> findCompletedByUser(
            @Param("userId") Long userId,
            @Param("status") SessionStatus status,
            Pageable pageable);

    /**
     * Finds historical sessions for a user.
     *
     * @param userId user id
     * @param now current instant
     * @param statuses terminal statuses
     * @param pageable page request
     * @return historical sessions
     */
    @Query("""
            select session
            from Session session
            where (session.mentor.id = :userId or session.learner.id = :userId)
              and (session.status in :statuses or session.endTime < :now)
            """)
    Page<Session> findHistoryByUser(
            @Param("userId") Long userId,
            @Param("now") Instant now,
            @Param("statuses") Collection<SessionStatus> statuses,
            Pageable pageable);

    /**
     * Finds cancelled sessions.
     *
     * @param status cancelled status
     * @param pageable page request
     * @return cancelled sessions
     */
    Page<Session> findByStatus(SessionStatus status, Pageable pageable);

    /**
     * Finds sessions on a date range.
     *
     * @param start inclusive range start
     * @param end exclusive range end
     * @param pageable page request
     * @return sessions in range
     */
    Page<Session> findByStartTimeGreaterThanEqualAndStartTimeLessThan(Instant start, Instant end, Pageable pageable);

    /**
     * Checks overlapping active sessions for either participant.
     *
     * @param mentorId mentor id
     * @param learnerId learner id
     * @param startTime requested start
     * @param endTime requested end
     * @param statuses statuses that block booking
     * @param excludedSessionId optional session id to exclude during reschedule
     * @return true when an overlapping session exists
     */
    @Query("""
            select count(session) > 0
            from Session session
            where (:excludedSessionId is null or session.id <> :excludedSessionId)
              and session.status in :statuses
              and (session.mentor.id = :mentorId
                   or session.learner.id = :mentorId
                   or session.mentor.id = :learnerId
                   or session.learner.id = :learnerId)
              and session.startTime < :endTime
              and session.endTime > :startTime
            """)
    boolean existsOverlappingSession(
            @Param("mentorId") Long mentorId,
            @Param("learnerId") Long learnerId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            @Param("statuses") Collection<SessionStatus> statuses,
            @Param("excludedSessionId") Long excludedSessionId);

    /**
     * Checks whether two users share an active session.
     *
     * @param firstUserId first user id
     * @param secondUserId second user id
     * @param statuses active session statuses
     * @return true when an active session exists
     */
    @Query("""
            select count(session) > 0
            from Session session
            where session.status in :statuses
              and ((session.mentor.id = :firstUserId and session.learner.id = :secondUserId)
                or (session.mentor.id = :secondUserId and session.learner.id = :firstUserId))
            """)
    boolean existsActiveBetweenUsers(
            @Param("firstUserId") Long firstUserId,
            @Param("secondUserId") Long secondUserId,
            @Param("statuses") Collection<SessionStatus> statuses);
}
