package com.skillswap.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for rotating a refresh token.
 */
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token is required")
        String refreshToken) {
}
