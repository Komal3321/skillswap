package com.skillswap.controller.booking;

import com.skillswap.common.api.ApiResponse;
import com.skillswap.dto.request.booking.CreateSessionRequest;
import com.skillswap.dto.request.booking.UpdateSessionRequest;
import com.skillswap.dto.response.booking.SessionHistoryResponse;
import com.skillswap.dto.response.booking.SessionResponse;
import com.skillswap.service.booking.SessionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
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
 * REST controller for session booking APIs.
 */
@Validated
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * Creates a session booking from an accepted skill request.
     *
     * @param request create request
     * @return created session
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SessionResponse>> createSession(
            @Valid @RequestBody CreateSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Session created successfully", sessionService.createSession(request)));
    }

    /**
     * Lists sessions visible to the authenticated user.
     *
     * @param page page number
     * @param size page size
     * @return paged sessions
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SessionResponse>>> getSessions(
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size) {
        return ResponseEntity.ok(ApiResponse.success("Sessions fetched successfully",
                sessionService.getSessions(page, size)));
    }

    /**
     * Gets one session visible to the authenticated user.
     *
     * @param id session id
     * @return session
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SessionResponse>> getSession(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Session fetched successfully", sessionService.getSession(id)));
    }

    /**
     * Updates a mutable session.
     *
     * @param id session id
     * @param request update request
     * @return updated session
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SessionResponse>> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSessionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Session updated successfully",
                sessionService.updateSession(id, request)));
    }

    /**
     * Accepts a pending booking.
     *
     * @param id session id
     * @return accepted session
     */
    @PutMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<SessionResponse>> acceptSession(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Session accepted successfully",
                sessionService.acceptSession(id)));
    }

    /**
     * Rejects a pending booking.
     *
     * @param id session id
     * @return rejected session
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<SessionResponse>> rejectSession(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Session rejected successfully",
                sessionService.rejectSession(id)));
    }

    /**
     * Cancels a mutable session.
     *
     * @param id session id
     * @return cancelled session
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SessionResponse>> cancelSession(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Session cancelled successfully",
                sessionService.cancelSession(id)));
    }

    /**
     * Reschedules a session as mentor.
     *
     * @param id session id
     * @param request reschedule request
     * @return rescheduled session
     */
    @PutMapping("/{id}/reschedule")
    public ResponseEntity<ApiResponse<SessionResponse>> rescheduleSession(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSessionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Session rescheduled successfully",
                sessionService.rescheduleSession(id, request)));
    }

    /**
     * Marks a session complete.
     *
     * @param id session id
     * @return completed session
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<SessionResponse>> completeSession(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Session completed successfully",
                sessionService.completeSession(id)));
    }

    /**
     * Lists upcoming sessions.
     *
     * @param page page number
     * @param size page size
     * @return upcoming sessions
     */
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<Page<SessionHistoryResponse>>> getUpcomingSessions(
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size) {
        return ResponseEntity.ok(ApiResponse.success("Upcoming sessions fetched successfully",
                sessionService.getUpcomingSessions(page, size)));
    }

    /**
     * Lists completed session history.
     *
     * @param page page number
     * @param size page size
     * @return session history
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<SessionHistoryResponse>>> getSessionHistory(
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size) {
        return ResponseEntity.ok(ApiResponse.success("Session history fetched successfully",
                sessionService.getSessionHistory(page, size)));
    }
}
