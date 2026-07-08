package com.skillswap.dto.response.profile;

import java.util.List;
import java.util.Set;

import com.skillswap.domain.entity.UserProfile.LearningMode;
import com.skillswap.domain.entity.UserSkill.ProficiencyLevel;
import com.skillswap.domain.entity.UserSkill.SkillType;

/**
 * Public user profile response.
 */
public record UserProfileResponse(
        Long userId,
        String fullName,
        String email,
        String phoneNumber,
        String bio,
        String profileImageUrl,
        String city,
        String country,
        String timeZone,
        LearningMode preferredLearningMode,
        String experience,
        Set<String> languages,
        Set<String> portfolioLinks,
        Set<String> portfolioDocumentUrls,
        Set<String> certificateUrls,
        List<UserSkillResponse> skillsOffered,
        List<UserSkillResponse> skillsWanted,
        List<AvailabilityResponse> availability) {

    /**
     * Skill attached to a profile.
     */
    public record UserSkillResponse(
            Long id,
            Long skillId,
            String skillName,
            SkillType skillType,
            ProficiencyLevel proficiencyLevel,
            Integer yearsOfExperience,
            String description,
            boolean verified) {
    }
}
