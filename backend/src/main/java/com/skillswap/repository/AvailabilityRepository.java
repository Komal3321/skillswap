package com.skillswap.repository;

import java.util.List;

import com.skillswap.domain.entity.Availability;

/**
 * Repository for weekly user availability.
 */
public interface AvailabilityRepository extends BaseRepository<Availability, Long> {

    /**
     * Lists active availability slots for a user ordered for calendar display.
     *
     * @param userId user identifier
     * @return ordered availability slots
     */
    List<Availability> findByUserIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(Long userId);

    /**
     * Removes all availability slots for a user before replacing the weekly schedule.
     *
     * @param userId user identifier
     */
    void deleteByUserId(Long userId);
}
