package com.skillswap.repository;

import java.util.List;

import com.skillswap.domain.entity.Availability.AvailabilityMode;
import com.skillswap.domain.entity.UserSkill;
import com.skillswap.domain.entity.UserSkill.ProficiencyLevel;
import com.skillswap.domain.entity.UserSkill.SkillType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for skills attached to users.
 */
public interface UserSkillRepository extends BaseRepository<UserSkill, Long> {

    /**
     * Lists all skills for a user.
     *
     * @param userId user identifier
     * @return user skills
     */
    List<UserSkill> findByUserId(Long userId);

    /**
     * Lists skills for a user with pagination.
     *
     * @param userId user identifier
     * @param pageable page request
     * @return user skills
     */
    Page<UserSkill> findByUserId(Long userId, Pageable pageable);

    /**
     * Deletes user skills for a specific type before replacing them.
     *
     * @param userId user identifier
     * @param skillType offered or wanted
     */
    void deleteByUserIdAndSkillType(Long userId, SkillType skillType);

    /**
     * Searches public user skill listings with optional filters.
     *
     * @param skillName skill name fragment
     * @param categoryId category id
     * @param city user city
     * @param language profile language
     * @param proficiencyLevel experience level
     * @param minRating minimum average review rating
     * @param availabilityMode online/offline/both mode
     * @param bothMode enum value used to include flexible availability
     * @param skillType offered or wanted skill type
     * @param pageable page request
     * @return matching user skill listings
     */
    @Query("""
            select distinct us
            from UserSkill us
            join us.skill s
            join s.category c
            join us.user u
            left join UserProfile p on p.user = u
            left join p.languages language
            where (:skillName is null or lower(s.name) like lower(concat('%', :skillName, '%')))
              and (:categoryId is null or c.id = :categoryId)
              and (:city is null or lower(p.city) = lower(:city))
              and (:language is null or lower(language) = lower(:language))
              and (:proficiencyLevel is null or us.proficiencyLevel = :proficiencyLevel)
              and (:skillType is null or us.skillType = :skillType)
              and (:availabilityMode is null or exists (
                    select 1
                    from Availability availability
                    where availability.user = u
                      and availability.active = true
                      and (availability.mode = :availabilityMode or availability.mode = :bothMode)
              ))
              and (:minRating is null or (
                    select avg(review.rating)
                    from Review review
                    where review.reviewee = u
              ) >= :minRating)
            """)
    Page<UserSkill> searchListings(
            @Param("skillName") String skillName,
            @Param("categoryId") Long categoryId,
            @Param("city") String city,
            @Param("language") String language,
            @Param("proficiencyLevel") ProficiencyLevel proficiencyLevel,
            @Param("minRating") Double minRating,
            @Param("availabilityMode") AvailabilityMode availabilityMode,
            @Param("bothMode") AvailabilityMode bothMode,
            @Param("skillType") SkillType skillType,
            Pageable pageable);

    /**
     * Calculates a user's average received rating.
     *
     * @param userId user identifier
     * @return average rating, or null when unrated
     */
    @Query("select avg(review.rating) from Review review where review.reviewee.id = :userId")
    Double averageRatingForUser(@Param("userId") Long userId);
}
