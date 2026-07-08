package com.skillswap.repository;

import java.util.List;

import com.skillswap.domain.entity.UserSkill;
import com.skillswap.domain.entity.UserSkill.SkillType;

/**
 * Repository for skills attached to users.
 */
public interface UserSkillRepository extends BaseRepository<UserSkill, Long> {

    /**
     * Lists all skills for a user.
     *
     * @param userId user identifier
     * @return user skills
     */
    List<UserSkill> findByUserId(Long userId);

    /**
     * Deletes user skills for a specific type before replacing them.
     *
     * @param userId user identifier
     * @param skillType offered or wanted
     */
    void deleteByUserIdAndSkillType(Long userId, SkillType skillType);
}
