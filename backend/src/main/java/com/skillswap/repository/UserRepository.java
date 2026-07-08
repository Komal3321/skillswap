package com.skillswap.repository;

import java.util.Optional;

import com.skillswap.domain.entity.User;

/**
 * Repository for querying SkillSwap users.
 */
public interface UserRepository extends BaseRepository<User, Long> {

    /**
     * Finds a user by email address.
     *
     * @param email normalized email address
     * @return matching user, when present
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether an email is already registered.
     *
     * @param email normalized email address
     * @return true when the email exists
     */
    boolean existsByEmail(String email);
}
