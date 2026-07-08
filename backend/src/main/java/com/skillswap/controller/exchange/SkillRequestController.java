package com.skillswap.controller.exchange;

import com.skillswap.common.api.ApiResponse;
import com.skillswap.domain.enums.RequestStatus;
import com.skillswap.dto.request.exchange.CreateSkillRequestDTO;
import com.skillswap.dto.request.exchange.UpdateSkillRequestDTO;
import com.skillswap.dto.response.exchange.RequestHistoryDTO;
import com.skillswap.dto.response.exchange.SkillRequestResponseDTO;
import com.skillswap.service.exchange.SkillRequestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * REST controller for skill exchange request APIs.
 */
@Validated
@RestController
@RequestMapping("/api/skill-requests")
public class SkillRequestController {

    private final SkillRequestService skillRequestService;

    public SkillRequestController(SkillRequestService skillRequestService) {
        this.skillRequestService = skillRequestService;
    }

    /**
     * Creates a skill exchange request as the authenticated learner.
     *
     * @param request create request
     * @return created request
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SkillRequestResponseDTO>> createRequest(
            @Valid @RequestBody CreateSkillRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Skill request created successfully",
                        skillRequestService.createRequest(request)));
    }

    /**
     * Lists all requests for administrators.
     *
     * @param status optional status filter
     * @param page page number
     * @param size page size
     * @param sortBy sort property
     * @param sortDirection sort direction
     * @return paged requests
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<SkillRequestResponseDTO>>> getRequests(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size,
            @RequestParam(required = false) @Size(max = 40) String sortBy,
            @RequestParam(required = false) @Size(max = 4) String sortDirection) {
        return ResponseEntity.ok(ApiResponse.success("Skill requests fetched successfully",
                skillRequestService.getAllRequests(status, page, size, sortBy, sortDirection)));
    }

    /**
     * Lists requests created by the authenticated learner.
     *
     * @param page page number
     * @param size page size
     * @return request history
     */
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<Page<RequestHistoryDTO>>> getUserRequests(
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size) {
        return ResponseEntity.ok(ApiResponse.success("User requests fetched successfully",
                skillRequestService.getUserRequests(page, size)));
    }

    /**
     * Lists requests received by the authenticated mentor.
     *
     * @param page page number
     * @param size page size
     * @return request history
     */
    @GetMapping("/mentor")
    public ResponseEntity<ApiResponse<Page<RequestHistoryDTO>>> getMentorRequests(
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size) {
        return ResponseEntity.ok(ApiResponse.success("Mentor requests fetched successfully",
                skillRequestService.getMentorRequests(page, size)));
    }

    /**
     * Lists pending requests received by the authenticated mentor.
     *
     * @param page page number
     * @param size page size
     * @return pending request history
     */
    @GetMapping("/mentor/pending")
    public ResponseEntity<ApiResponse<Page<RequestHistoryDTO>>> getPendingRequests(
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size) {
        return ResponseEntity.ok(ApiResponse.success("Pending requests fetched successfully",
                skillRequestService.getPendingRequests(page, size)));
    }

    /**
     * Lists completed requests involving the authenticated user.
     *
     * @param page page number
     * @param size page size
     * @return completed request history
     */
    @GetMapping("/completed")
    public ResponseEntity<ApiResponse<Page<RequestHistoryDTO>>> getCompletedRequests(
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size) {
        return ResponseEntity.ok(ApiResponse.success("Completed requests fetched successfully",
                skillRequestService.getCompletedRequests(page, size)));
    }

    /**
     * Gets a request visible to the authenticated participant or administrator.
     *
     * @param id request id
     * @return request
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SkillRequestResponseDTO>> getRequest(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Skill request fetched successfully",
                skillRequestService.getRequest(id)));
    }

    /**
     * Accepts a pending request as its mentor.
     *
     * @param id request id
     * @param request optional update request
     * @return accepted request
     */
    @PutMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<SkillRequestResponseDTO>> acceptRequest(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) UpdateSkillRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Skill request accepted successfully",
                skillRequestService.acceptRequest(id, request)));
    }

    /**
     * Rejects a pending request as its mentor.
     *
     * @param id request id
     * @param request optional update request
     * @return rejected request
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<SkillRequestResponseDTO>> rejectRequest(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) UpdateSkillRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Skill request rejected successfully",
                skillRequestService.rejectRequest(id, request)));
    }

    /**
     * Cancels a pending request as its learner.
     *
     * @param id request id
     * @return cancelled request
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SkillRequestResponseDTO>> cancelRequest(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Skill request cancelled successfully",
                skillRequestService.cancelRequest(id)));
    }

    /**
     * Completes an accepted request as its mentor.
     *
     * @param id request id
     * @return completed request
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<SkillRequestResponseDTO>> completeRequest(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Skill request completed successfully",
                skillRequestService.completeRequest(id)));
    }
}
