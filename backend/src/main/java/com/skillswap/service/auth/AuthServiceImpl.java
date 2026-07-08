package com.skillswap.service.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.skillswap.common.exception.BadRequestException;
import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.domain.entity.Role;
import com.skillswap.domain.entity.Role.RoleName;
import com.skillswap.domain.entity.User;
import com.skillswap.dto.request.auth.ForgotPasswordRequest;
import com.skillswap.dto.request.auth.LoginRequest;
import com.skillswap.dto.request.auth.LogoutRequest;
import com.skillswap.dto.request.auth.RefreshTokenRequest;
import com.skillswap.dto.request.auth.RegisterRequest;
import com.skillswap.dto.request.auth.ResetPasswordRequest;
import com.skillswap.dto.response.auth.AuthResponse;
import com.skillswap.dto.response.auth.MessageResponse;
import com.skillswap.dto.response.auth.UserSummaryResponse;
import com.skillswap.repository.RoleRepository;
import com.skillswap.repository.UserRepository;
import com.skillswap.security.jwt.JwtProperties;
import com.skillswap.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default authentication service implementation using JPA, Redis, BCrypt, and JWT.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final String TOKEN_TYPE = "Bearer";
    private static final String REFRESH_KEY_PREFIX = "auth:refresh:";
    private static final String ACCESS_BLACKLIST_PREFIX = "auth:blacklist:access:";
    private static final String PASSWORD_RESET_PREFIX = "auth:password-reset:";
    private static final String EMAIL_VERIFY_PREFIX = "auth:email-verify:";
    private static final Duration PASSWORD_RESET_TTL = Duration.ofMinutes(30);
    private static final Duration EMAIL_VERIFICATION_TTL = Duration.ofDays(1);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final StringRedisTemplate redisTemplate;

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            JwtProperties jwtProperties,
            StringRedisTemplate redisTemplate) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email is already registered");
        }

        Set<Role> roles = resolveRoles(request.roles());
        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .phoneNumber(normalizeNullable(request.phoneNumber()))
                .enabled(true)
                .emailVerified(false)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);
        createEmailVerificationToken(savedUser);
        return issueTokens(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (BadCredentialsException exception) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        return issueTokens(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();
        if (!jwtTokenProvider.isTokenValid(refreshToken)) {
            throw new JwtException("Invalid refresh token");
        }

        String tokenKey = refreshTokenKey(refreshToken);
        String email = redisTemplate.opsForValue().get(tokenKey);
        if (email == null) {
            throw new JwtException("Refresh token is expired or revoked");
        }

        redisTemplate.delete(tokenKey);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return issueTokens(user);
    }

    @Override
    public MessageResponse logout(LogoutRequest request, String accessToken) {
        redisTemplate.delete(refreshTokenKey(request.refreshToken()));
        blacklistAccessToken(accessToken);
        return new MessageResponse("Logged out successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.email());
        userRepository.findByEmail(email).ifPresent(this::createPasswordResetToken);
        return new MessageResponse("If the email exists, password reset instructions will be sent");
    }

    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        String tokenKey = PASSWORD_RESET_PREFIX + hash(request.token());
        String email = redisTemplate.opsForValue().get(tokenKey);
        if (email == null) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        redisTemplate.delete(tokenKey);
        return new MessageResponse("Password reset successfully");
    }

    @Override
    @Transactional
    public MessageResponse verifyEmail(String email, String token) {
        String normalizedEmail = normalizeEmail(email);
        String tokenKey = EMAIL_VERIFY_PREFIX + hash(token);
        String storedEmail = redisTemplate.opsForValue().get(tokenKey);
        if (storedEmail == null || !storedEmail.equals(normalizedEmail)) {
            throw new BadRequestException("Invalid or expired email verification token");
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEmailVerified(true);
        userRepository.save(user);
        redisTemplate.delete(tokenKey);
        return new MessageResponse("Email verified successfully");
    }

    private AuthResponse issueTokens(User user) {
        Map<String, Object> claims = buildClaims(user);
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), claims);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail(), claims);
        redisTemplate.opsForValue().set(
                refreshTokenKey(refreshToken),
                user.getEmail(),
                Duration.ofDays(jwtProperties.refreshTokenExpirationDays()));
        return new AuthResponse(
                accessToken,
                refreshToken,
                TOKEN_TYPE,
                Duration.ofMinutes(jwtProperties.accessTokenExpirationMinutes()).toSeconds(),
                toUserSummary(user));
    }

    private Map<String, Object> buildClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("roles", user.getRoles()
                .stream()
                .map(role -> role.getName().name())
                .sorted()
                .toList());
        claims.put("emailVerified", user.isEmailVerified());
        return claims;
    }

    private UserSummaryResponse toUserSummary(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.isEmailVerified(),
                user.getRoles()
                        .stream()
                        .map(Role::getName)
                        .map(RoleName::name)
                        .collect(Collectors.toUnmodifiableSet()));
    }

    private Set<Role> resolveRoles(Set<RoleName> requestedRoles) {
        return requestedRoles.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new BadRequestException("Role is not configured: " + roleName.name())))
                .collect(Collectors.toSet());
    }

    private void createPasswordResetToken(User user) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(PASSWORD_RESET_PREFIX + hash(token), user.getEmail(), PASSWORD_RESET_TTL);
        // Placeholder: integrate email provider and send token to the user.
    }

    private void createEmailVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(EMAIL_VERIFY_PREFIX + hash(token), user.getEmail(), EMAIL_VERIFICATION_TTL);
        // Placeholder: integrate email provider and send token to the user.
    }

    private void blacklistAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return;
        }
        try {
            Date expiration = jwtTokenProvider.extractExpiration(accessToken);
            Duration ttl = Duration.between(Instant.now(), expiration.toInstant());
            if (!ttl.isNegative() && !ttl.isZero()) {
                redisTemplate.opsForValue().set(ACCESS_BLACKLIST_PREFIX + hash(accessToken), "revoked", ttl);
            }
        } catch (JwtException ignored) {
            // Invalid access tokens do not need blacklist state.
        }
    }

    private String refreshTokenKey(String token) {
        return REFRESH_KEY_PREFIX + hash(token);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hashed.length * 2);
            for (byte value : hashed) {
                builder.append(String.format("%02x", value & 0xff));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
