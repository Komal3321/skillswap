package com.skillswap.service.auth;

import com.skillswap.dto.request.auth.ForgotPasswordRequest;
import com.skillswap.dto.request.auth.LoginRequest;
import com.skillswap.dto.request.auth.LogoutRequest;
import com.skillswap.dto.request.auth.RefreshTokenRequest;
import com.skillswap.dto.request.auth.RegisterRequest;
import com.skillswap.dto.request.auth.ResetPasswordRequest;
import com.skillswap.dto.response.auth.AuthResponse;
import com.skillswap.dto.response.auth.MessageResponse;

/**
 * Application service contract for authentication and account recovery flows.
 */
public interface AuthService {

    /**
     * Registers a user and issues initial JWT credentials.
     *
     * @param request registration details
     * @return issued credentials and user summary
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates credentials and issues JWT credentials.
     *
     * @param request login details
     * @return issued credentials and user summary
     */
    AuthResponse login(LoginRequest request);

    /**
     * Rotates a valid refresh token.
     *
     * @param request refresh token request
     * @return replacement credentials
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * Invalidates the supplied refresh token and current access token.
     *
     * @param request logout body
     * @param accessToken bearer access token, when provided
     * @return logout result
     */
    MessageResponse logout(LogoutRequest request, String accessToken);

    /**
     * Starts the forgot-password flow.
     *
     * @param request forgot-password request
     * @return generic response
     */
    MessageResponse forgotPassword(ForgotPasswordRequest request);

    /**
     * Completes password reset using a reset token.
     *
     * @param request reset-password request
     * @return reset result
     */
    MessageResponse resetPassword(ResetPasswordRequest request);

    /**
     * Verifies a user's email address.
     *
     * @param email user email
     * @param token verification token
     * @return verification result
     */
    MessageResponse verifyEmail(String email, String token);
}
