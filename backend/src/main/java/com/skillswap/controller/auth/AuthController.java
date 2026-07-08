package com.skillswap.controller.auth;

import com.skillswap.common.api.ApiResponse;
import com.skillswap.dto.request.auth.ForgotPasswordRequest;
import com.skillswap.dto.request.auth.LoginRequest;
import com.skillswap.dto.request.auth.LogoutRequest;
import com.skillswap.dto.request.auth.RefreshTokenRequest;
import com.skillswap.dto.request.auth.RegisterRequest;
import com.skillswap.dto.request.auth.ResetPasswordRequest;
import com.skillswap.dto.response.auth.AuthResponse;
import com.skillswap.dto.response.auth.MessageResponse;
import com.skillswap.service.auth.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing SkillSwap authentication APIs.
 */
@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user.
     *
     * @param request registration request
     * @return JWT credentials and user summary
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", authService.register(request)));
    }

    /**
     * Authenticates a user.
     *
     * @param request login request
     * @return JWT credentials and user summary
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
    }

    /**
     * Rotates a refresh token and returns replacement credentials.
     *
     * @param request refresh request
     * @return replacement JWT credentials
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", authService.refreshToken(request)));
    }

    /**
     * Logs out the current user by revoking token state.
     *
     * @param request logout request
     * @param authorization authorization header
     * @return logout result
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<MessageResponse>> logout(
            @Valid @RequestBody LogoutRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        MessageResponse response = authService.logout(request, resolveBearerToken(authorization));
        return ResponseEntity.ok(ApiResponse.success(response.message(), response));
    }

    /**
     * Starts the forgot-password flow.
     *
     * @param request forgot-password request
     * @return generic response
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<MessageResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        MessageResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(response.message(), response));
    }

    /**
     * Resets a password using a reset token.
     *
     * @param request reset-password request
     * @return reset result
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<MessageResponse>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        MessageResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(response.message(), response));
    }

    /**
     * Placeholder email verification endpoint.
     *
     * @param email account email
     * @param token verification token
     * @return verification result
     */
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<MessageResponse>> verifyEmail(
            @RequestParam @NotBlank @Email String email,
            @RequestParam @NotBlank String token) {
        MessageResponse response = authService.verifyEmail(email, token);
        return ResponseEntity.ok(ApiResponse.success(response.message(), response));
    }

    private String resolveBearerToken(String authorization) {
        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
