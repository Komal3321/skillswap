package com.skillswap.dto.response.skill;

/**
 * Response DTO for a marketplace skill.
 */
public record SkillResponse(
        Long id,
        String name,
        String description,
        Long categoryId,
        String categoryName) {
}
