package com.skillswap.dto.response.skill;

/**
 * Response DTO for a skill category.
 */
public record CategoryResponse(
        Long id,
        String name,
        String description,
        long skillCount) {
}
