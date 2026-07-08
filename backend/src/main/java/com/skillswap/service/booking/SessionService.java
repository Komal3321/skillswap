package com.skillswap.service.booking;

import com.skillswap.dto.request.booking.CreateSessionRequest;
import com.skillswap.dto.request.booking.UpdateSessionRequest;
import com.skillswap.dto.response.booking.SessionHistoryResponse;
import com.skillswap.dto.response.booking.SessionResponse;
import org.springframework.data.domain.Page;

/**
 * Application service for booking and session lifecycle use cases.
 */
public interface SessionService {

    /**
     * Creates a pending session booking for an accepted skill request.
     *
     * @param request create request
     * @return created session
     */
    SessionResponse createSession(CreateSessionRequest request);

    /**
     * Updates editable session details.
     *
     * @param id session id
     * @param request update request
     * @return updated session
     */
    SessionResponse updateSession(Long id, UpdateSessionRequest request);

    /**
     * Accepts a pending or rescheduled session as mentor.
     *
     * @param id session id
     * @return accepted session
     */
    SessionResponse acceptSession(Long id);

    /**
     * Rejects a pending session as mentor.
     *
     * @param id session id
     * @return rejected session
     */
    SessionResponse rejectSession(Long id);

    /**
     * Cancels a session as learner, mentor, or admin.
     *
     * @param id session id
     * @return cancelled session
     */
    SessionResponse cancelSession(Long id);

    /**
     * Reschedules a session as mentor.
     *
     * @param id session id
     * @param request reschedule request
     * @return rescheduled session
     */
    SessionResponse rescheduleSession(Long id, UpdateSessionRequest request);

    /**
     * Completes an accepted session as mentor.
     *
     * @param id session id
     * @return completed session
     */
    SessionResponse completeSession(Long id);

    /**
     * Gets a session visible to the current user.
     *
     * @param id session id
     * @return session
     */
    SessionResponse getSession(Long id);

    /**
     * Lists sessions visible to the current user.
     *
     * @param page page number
     * @param size page size
     * @return paged sessions
     */
    Page<SessionResponse> getSessions(Integer page, Integer size);

    /**
     * Lists upcoming sessions for the current user.
     *
     * @param page page number
     * @param size page size
     * @return upcoming sessions
     */
    Page<SessionHistoryResponse> getUpcomingSessions(Integer page, Integer size);

    /**
     * Lists historical sessions for the current user.
     *
     * @param page page number
     * @param size page size
     * @return historical sessions
     */
    Page<SessionHistoryResponse> getSessionHistory(Integer page, Integer size);
}
