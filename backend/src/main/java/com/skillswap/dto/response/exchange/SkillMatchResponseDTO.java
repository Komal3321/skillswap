package com.skillswap.dto.response.exchange;

/**
 * Response DTO describing mentor-learner skill compatibility.
 */
public record SkillMatchResponseDTO(
        boolean mentorCanTeachRequestedSkill,
        boolean mentorWantsOfferedSkill,
        boolean learnerCanOfferSkill,
        int score,
        String summary) {
}
