package com.skillswap.dto.request.profile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for updating the authenticated user's profile.
 */
public record UpdateProfileRequest(
        @NotNull @Valid UserProfileRequest profile) {
}
