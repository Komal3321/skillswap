package com.skillswap.service.profile;

import java.util.List;

import com.skillswap.dto.request.profile.AvailabilityRequest;
import com.skillswap.dto.request.profile.UpdateProfileRequest;
import com.skillswap.dto.response.profile.AvailabilityResponse;
import com.skillswap.dto.response.profile.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Application service for user profile use cases.
 */
public interface UserProfileService {

    /**
     * Returns the authenticated user's profile.
     *
     * @return profile response
     */
    UserProfileResponse getCurrentUserProfile();

    /**
     * Returns a profile by user id.
     *
     * @param userId user identifier
     * @return profile response
     */
    UserProfileResponse getUserProfile(Long userId);

    /**
     * Updates the authenticated user's editable profile.
     *
     * @param request update request
     * @return updated profile response
     */
    UserProfileResponse updateCurrentUserProfile(UpdateProfileRequest request);

    /**
     * Uploads and stores a profile photo for the authenticated user.
     *
     * @param file uploaded image
     * @return updated profile response
     */
    UserProfileResponse uploadProfilePhoto(MultipartFile file);

    /**
     * Uploads and stores a certificate document for the authenticated user.
     *
     * @param file uploaded certificate
     * @return updated profile response
     */
    UserProfileResponse uploadCertificate(MultipartFile file);

    /**
     * Uploads and stores a portfolio document for the authenticated user.
     *
     * @param file uploaded portfolio document
     * @return updated profile response
     */
    UserProfileResponse uploadPortfolioDocument(MultipartFile file);

    /**
     * Returns the authenticated user's weekly availability.
     *
     * @return availability slots
     */
    List<AvailabilityResponse> getCurrentUserAvailability();

    /**
     * Replaces the authenticated user's weekly availability.
     *
     * @param requests requested slots
     * @return saved slots
     */
    List<AvailabilityResponse> updateCurrentUserAvailability(List<AvailabilityRequest> requests);
}
