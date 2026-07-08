package com.skillswap.dto.response.auth;

/**
 * Response containing issued JWT credentials and user profile summary.
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        UserSummaryResponse user) {
}
