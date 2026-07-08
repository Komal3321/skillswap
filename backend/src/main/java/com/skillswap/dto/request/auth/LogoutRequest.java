package com.skillswap.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for invalidating a refresh token.
 */
public record LogoutRequest(
        @NotBlank(message = "Refresh token is required")
        String refreshToken) {
}
