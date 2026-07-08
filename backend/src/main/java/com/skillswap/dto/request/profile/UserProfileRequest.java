package com.skillswap.dto.request.profile;

import java.util.Set;

import com.skillswap.domain.entity.UserProfile.LearningMode;
import com.skillswap.domain.entity.UserSkill.ProficiencyLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

/**
 * Complete profile payload used to replace editable profile details.
 */
public record UserProfileRequest(
        @Size(max = 100) String fullName,
        @Size(max = 30) String phoneNumber,
        @Size(max = 500) String bio,
        @Size(max = 120) String city,
        @Size(max = 120) String country,
        @Size(max = 80) String timeZone,
        LearningMode preferredLearningMode,
        @Size(max = 1000) String experience,
        @Size(max = 20) Set<@Size(max = 80) String> languages,
        @Size(max = 20) Set<@URL @Size(max = 500) String> portfolioLinks,
        @Size(max = 50) Set<@Valid SkillSelectionRequest> skillsOffered,
        @Size(max = 50) Set<@Valid SkillSelectionRequest> skillsWanted) {

    /**
     * Skill selection submitted as part of a profile update.
     */
    public record SkillSelectionRequest(
            @NotNull @Positive Long skillId,
            @NotNull ProficiencyLevel proficiencyLevel,
            @NotNull @Min(0) @Max(80) Integer yearsOfExperience,
            @Size(max = 500) String description) {
    }
}
