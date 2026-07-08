package com.skillswap.dto.request.auth;

import java.util.Set;

import com.skillswap.domain.entity.Role.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * Request body for registering a new user.
 */
public record RegisterRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must be 100 characters or fewer")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 150, message = "Email must be 150 characters or fewer")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        String password,

        @Size(max = 30, message = "Phone number must be 30 characters or fewer")
        String phoneNumber,

        @NotEmpty(message = "At least one role is required")
        Set<RoleName> roles) {
}
