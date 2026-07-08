package com.skillswap.repository;

import java.util.Optional;

import com.skillswap.domain.entity.Role;
import com.skillswap.domain.entity.Role.RoleName;

/**
 * Repository for role lookup and assignment.
 */
public interface RoleRepository extends BaseRepository<Role, Long> {

    /**
     * Finds a role by its enum name.
     *
     * @param name role name
     * @return matching role, when present
     */
    Optional<Role> findByName(RoleName name);
}
