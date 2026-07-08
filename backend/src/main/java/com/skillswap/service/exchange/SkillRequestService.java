package com.skillswap.service.exchange;

import com.skillswap.domain.enums.RequestStatus;
import com.skillswap.dto.request.exchange.CreateSkillRequestDTO;
import com.skillswap.dto.request.exchange.UpdateSkillRequestDTO;
import com.skillswap.dto.response.exchange.RequestHistoryDTO;
import com.skillswap.dto.response.exchange.SkillRequestResponseDTO;
import org.springframework.data.domain.Page;

/**
 * Application service for skill exchange request use cases.
 */
public interface SkillRequestService {

    /**
     * Creates a skill exchange request for the authenticated learner.
     *
     * @param request create request
     * @return created request
     */
    SkillRequestResponseDTO createRequest(CreateSkillRequestDTO request);

    /**
     * Accepts a pending request as the mentor.
     *
     * @param requestId request id
     * @param request optional schedule suggestion
     * @return accepted request
     */
    SkillRequestResponseDTO acceptRequest(Long requestId, UpdateSkillRequestDTO request);

    /**
     * Rejects a pending request as the mentor.
     *
     * @param requestId request id
     * @param request optional rejection message
     * @return rejected request
     */
    SkillRequestResponseDTO rejectRequest(Long requestId, UpdateSkillRequestDTO request);

    /**
     * Cancels a pending request as its learner.
     *
     * @param requestId request id
     * @return cancelled request
     */
    SkillRequestResponseDTO cancelRequest(Long requestId);

    /**
     * Completes an accepted request as the mentor.
     *
     * @param requestId request id
     * @return completed request
     */
    SkillRequestResponseDTO completeRequest(Long requestId);

    /**
     * Gets a request visible to the authenticated user.
     *
     * @param requestId request id
     * @return request
     */
    SkillRequestResponseDTO getRequest(Long requestId);

    /**
     * Gets all requests for administrators.
     *
     * @param status optional status filter
     * @param page page number
     * @param size page size
     * @param sortBy sort property
     * @param sortDirection sort direction
     * @return paged requests
     */
    Page<SkillRequestResponseDTO> getAllRequests(
            RequestStatus status,
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection);

    /**
     * Gets requests created by the authenticated learner.
     *
     * @param page page number
     * @param size page size
     * @return request history
     */
    Page<RequestHistoryDTO> getUserRequests(Integer page, Integer size);

    /**
     * Gets requests received by the authenticated mentor.
     *
     * @param page page number
     * @param size page size
     * @return request history
     */
    Page<RequestHistoryDTO> getMentorRequests(Integer page, Integer size);

    /**
     * Gets pending requests received by the authenticated mentor.
     *
     * @param page page number
     * @param size page size
     * @return pending requests
     */
    Page<RequestHistoryDTO> getPendingRequests(Integer page, Integer size);

    /**
     * Gets completed requests involving the authenticated user.
     *
     * @param page page number
     * @param size page size
     * @return completed requests
     */
    Page<RequestHistoryDTO> getCompletedRequests(Integer page, Integer size);
}
