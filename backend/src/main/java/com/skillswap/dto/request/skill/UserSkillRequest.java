package com.skillswap.dto.request.skill;

import com.skillswap.domain.entity.UserSkill.ProficiencyLevel;
import com.skillswap.domain.entity.UserSkill.SkillType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request payload for adding a skill to the authenticated user's profile.
 */
public record UserSkillRequest(
        @NotNull @Positive Long skillId,
        @NotNull SkillType skillType,
        @NotNull ProficiencyLevel proficiencyLevel,
        @NotNull @Min(0) @Max(80) Integer yearsOfExperience,
        @Size(max = 500) String description) {
}
