package com.skillswap.repository;

import java.util.Optional;

import com.skillswap.domain.entity.UserProfile;

/**
 * Repository for user profile details.
 */
public interface UserProfileRepository extends BaseRepository<UserProfile, Long> {

    /**
     * Finds a profile owned by a user.
     *
     * @param userId user identifier
     * @return profile when present
     */
    Optional<UserProfile> findByUserId(Long userId);
}
