package com.skillswap.dto.request.skill;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for category create and update operations.
 */
public record CategoryRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description) {
}
