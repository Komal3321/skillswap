package com.skillswap.repository;

import java.util.Optional;

import com.skillswap.domain.entity.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Repository for skills.
 */
public interface SkillRepository extends BaseRepository<Skill, Long> {

    /**
     * Checks whether a skill name already exists.
     *
     * @param name skill name
     * @return true when present
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Finds a skill by case-insensitive name.
     *
     * @param name skill name
     * @return skill when present
     */
    Optional<Skill> findByNameIgnoreCase(String name);

    /**
     * Searches skills by name.
     *
     * @param name skill name fragment
     * @param pageable page request
     * @return matching skills
     */
    Page<Skill> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
