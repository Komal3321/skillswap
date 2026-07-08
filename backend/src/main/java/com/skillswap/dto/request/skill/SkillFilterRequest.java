package com.skillswap.dto.request.skill;

import com.skillswap.domain.entity.Availability.AvailabilityMode;
import com.skillswap.domain.entity.UserSkill.ProficiencyLevel;
import com.skillswap.domain.entity.UserSkill.SkillType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request body for advanced skill listing filters.
 */
public record SkillFilterRequest(
        @Size(max = 120) String skillName,
        @Positive Long categoryId,
        @Size(max = 120) String city,
        @Size(max = 80) String language,
        ProficiencyLevel experienceLevel,
        @Min(1) @Max(5) Double minRating,
        AvailabilityMode availabilityMode,
        SkillType skillType,
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size,
        @Size(max = 40) String sortBy,
        @Size(max = 4) String sortDirection) {
}
