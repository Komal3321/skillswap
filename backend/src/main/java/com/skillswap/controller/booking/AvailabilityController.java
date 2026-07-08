package com.skillswap.controller.booking;

import java.time.LocalDate;
import java.util.List;

import com.skillswap.common.api.ApiResponse;
import com.skillswap.domain.enums.SessionType;
import com.skillswap.dto.request.booking.AvailabilityRequest;
import com.skillswap.dto.response.booking.AvailabilityResponse;
import com.skillswap.dto.response.booking.TimeSlotResponse;
import com.skillswap.service.booking.AvailabilityService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for mentor availability APIs.
 */
@Validated
@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    /**
     * Adds one availability slot for the authenticated mentor.
     *
     * @param request availability request
     * @return saved availability
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AvailabilityResponse>> addAvailability(
            @Valid @RequestBody AvailabilityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Availability saved successfully",
                        availabilityService.addAvailability(request)));
    }

    /**
     * Replaces availability for the authenticated mentor.
     *
     * @param requests availability requests
     * @return saved availability
     */
    @PutMapping
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>> saveAvailability(
            @Valid @RequestBody List<@Valid AvailabilityRequest> requests) {
        return ResponseEntity.ok(ApiResponse.success("Availability updated successfully",
                availabilityService.saveAvailability(requests)));
    }

    /**
     * Gets availability for the authenticated mentor.
     *
     * @return current mentor availability
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>> getAvailability() {
        return ResponseEntity.ok(ApiResponse.success("Availability fetched successfully",
                availabilityService.getCurrentUserAvailability()));
    }

    /**
     * Gets availability for a mentor.
     *
     * @param mentorId mentor id
     * @return mentor availability
     */
    @GetMapping("/{mentorId}")
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>> getMentorAvailability(@PathVariable Long mentorId) {
        return ResponseEntity.ok(ApiResponse.success("Availability fetched successfully",
                availabilityService.getMentorAvailability(mentorId)));
    }

    /**
     * Gets concrete available slots for a mentor.
     *
     * @param mentorId mentor id
     * @param date target date
     * @param durationMinutes requested slot duration
     * @param meetingType requested meeting type
     * @return available slots
     */
    @GetMapping("/{mentorId}/slots")
    public ResponseEntity<ApiResponse<List<TimeSlotResponse>>> getAvailableSlots(
            @PathVariable Long mentorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @Min(15) @Max(480) Integer durationMinutes,
            @RequestParam(required = false) SessionType meetingType) {
        return ResponseEntity.ok(ApiResponse.success("Available slots fetched successfully",
                availabilityService.getAvailableSlots(mentorId, date, durationMinutes, meetingType)));
    }
}
