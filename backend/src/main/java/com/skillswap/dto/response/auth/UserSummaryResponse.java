package com.skillswap.dto.response.auth;

import java.util.Set;

/**
 * Public authenticated-user summary returned by auth APIs.
 */
public record UserSummaryResponse(
        Long id,
        String fullName,
        String email,
        boolean emailVerified,
        Set<String> roles) {
}
