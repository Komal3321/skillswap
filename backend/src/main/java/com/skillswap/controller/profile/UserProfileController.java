package com.skillswap.controller.profile;

import java.util.List;

import com.skillswap.common.api.ApiResponse;
import com.skillswap.dto.request.profile.AvailabilityRequest;
import com.skillswap.dto.request.profile.UpdateProfileRequest;
import com.skillswap.dto.response.profile.AvailabilityResponse;
import com.skillswap.dto.response.profile.UserProfileResponse;
import com.skillswap.service.profile.UserProfileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for user profile APIs.
 */
@Validated
@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    /**
     * Gets the authenticated user's profile.
     *
     * @return current user profile
     */
    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentProfile() {
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully",
                userProfileService.getCurrentUserProfile()));
    }

    /**
     * Gets any user profile. Restricted to administrators.
     *
     * @param userId target user id
     * @return user profile
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfileByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully",
                userProfileService.getUserProfile(userId)));
    }

    /**
     * Updates the authenticated user's profile.
     *
     * @param request profile update request
     * @return updated profile
     */
    @PutMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateCurrentProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully",
                userProfileService.updateCurrentUserProfile(request)));
    }

    /**
     * Uploads the authenticated user's profile photo.
     *
     * @param file uploaded image
     * @return updated profile
     */
    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadPhoto(
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Profile photo uploaded successfully",
                        userProfileService.uploadProfilePhoto(file)));
    }

    /**
     * Uploads a certificate document for the authenticated user.
     *
     * @param file uploaded certificate
     * @return updated profile
     */
    @PostMapping(value = "/certificate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadCertificate(
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Certificate uploaded successfully",
                        userProfileService.uploadCertificate(file)));
    }

    /**
     * Uploads a portfolio document for the authenticated user.
     *
     * @param file uploaded portfolio document
     * @return updated profile
     */
    @PostMapping(value = "/portfolio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadPortfolio(
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Portfolio document uploaded successfully",
                        userProfileService.uploadPortfolioDocument(file)));
    }

    /**
     * Gets the authenticated user's weekly availability.
     *
     * @return availability slots
     */
    @GetMapping("/availability")
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>> getAvailability() {
        return ResponseEntity.ok(ApiResponse.success("Availability fetched successfully",
                userProfileService.getCurrentUserAvailability()));
    }

    /**
     * Replaces the authenticated user's weekly availability.
     *
     * @param requests availability slots
     * @return saved availability slots
     */
    @PutMapping("/availability")
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>> updateAvailability(
            @Valid @NotNull @RequestBody List<@Valid AvailabilityRequest> requests) {
        return ResponseEntity.ok(ApiResponse.success("Availability updated successfully",
                userProfileService.updateCurrentUserAvailability(requests)));
    }
}
