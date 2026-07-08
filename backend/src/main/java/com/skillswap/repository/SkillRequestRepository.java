package com.skillswap.repository;

import java.time.Instant;
import java.util.Collection;

import com.skillswap.domain.entity.SkillRequest;
import com.skillswap.domain.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for skill exchange requests.
 */
public interface SkillRequestRepository extends BaseRepository<SkillRequest, Long> {

    /**
     * Checks for an existing active duplicate request.
     *
     * @param requesterId requester id
     * @param providerId provider id
     * @param skillId requested skill id
     * @return true when a duplicate active request exists
     */
    boolean existsByRequesterIdAndProviderIdAndSkillIdAndStatusIn(
            Long requesterId,
            Long providerId,
            Long skillId,
            Collection<RequestStatus> statuses);

    /**
     * Finds requests created by a user.
     *
     * @param requesterId requester id
     * @param pageable page request
     * @return matching requests
     */
    Page<SkillRequest> findByRequesterId(Long requesterId, Pageable pageable);

    /**
     * Finds requests received by a mentor.
     *
     * @param providerId mentor id
     * @param pageable page request
     * @return matching requests
     */
    Page<SkillRequest> findByProviderId(Long providerId, Pageable pageable);

    /**
     * Finds requests by requester or mentor.
     *
     * @param requesterId requester id
     * @param providerId provider id
     * @param pageable page request
     * @return matching requests
     */
    Page<SkillRequest> findByRequesterIdOrProviderId(Long requesterId, Long providerId, Pageable pageable);

    /**
     * Finds requests by status.
     *
     * @param status request status
     * @param pageable page request
     * @return matching requests
     */
    Page<SkillRequest> findByStatus(RequestStatus status, Pageable pageable);

    /**
     * Finds pending requests received by a mentor.
     *
     * @param providerId mentor id
     * @param pageable page request
     * @return pending requests
     */
    Page<SkillRequest> findByProviderIdAndStatus(Long providerId, RequestStatus status, Pageable pageable);

    /**
     * Finds accepted requests for a user.
     *
     * @param userId user id
     * @param status accepted status
     * @param pageable page request
     * @return accepted requests
     */
    @Query("""
            select request
            from SkillRequest request
            where (request.requester.id = :userId or request.provider.id = :userId)
              and request.status = :status
            """)
    Page<SkillRequest> findAcceptedRequestsByUser(
            @Param("userId") Long userId,
            @Param("status") RequestStatus status,
            Pageable pageable);

    /**
     * Finds completed requests for a user.
     *
     * @param userId user id
     * @param status completed status
     * @param pageable page request
     * @return completed requests
     */
    @Query("""
            select request
            from SkillRequest request
            where (request.requester.id = :userId or request.provider.id = :userId)
              and request.status = :status
            """)
    Page<SkillRequest> findCompletedRequestsByUser(
            @Param("userId") Long userId,
            @Param("status") RequestStatus status,
            Pageable pageable);

    /**
     * Finds expired requests by expiry time.
     *
     * @param status current status
     * @param now current instant
     * @param pageable page request
     * @return expired pending requests
     */
    Page<SkillRequest> findByStatusAndExpiresAtBefore(RequestStatus status, Instant now, Pageable pageable);

    /**
     * Marks stale pending requests as expired.
     *
     * @param pendingStatus pending status
     * @param expiredStatus expired status
     * @param now current instant
     * @return number of updated requests
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update SkillRequest request
            set request.status = :expiredStatus
            where request.status = :pendingStatus
              and request.expiresAt is not null
              and request.expiresAt < :now
            """)
    int expirePendingRequests(
            @Param("pendingStatus") RequestStatus pendingStatus,
            @Param("expiredStatus") RequestStatus expiredStatus,
            @Param("now") Instant now);
}
