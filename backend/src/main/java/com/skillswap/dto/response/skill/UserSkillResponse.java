package com.skillswap.dto.response.skill;

import com.skillswap.domain.entity.UserSkill.ProficiencyLevel;
import com.skillswap.domain.entity.UserSkill.SkillType;

/**
 * Response DTO for a user skill listing.
 */
public record UserSkillResponse(
        Long id,
        Long userId,
        String userFullName,
        String city,
        Long skillId,
        String skillName,
        Long categoryId,
        String categoryName,
        SkillType skillType,
        ProficiencyLevel proficiencyLevel,
        Integer yearsOfExperience,
        String description,
        boolean verified,
        Double averageRating) {
}
