package com.skillswap.service.skill;

import com.skillswap.dto.request.skill.CategoryRequest;
import com.skillswap.dto.request.skill.SkillFilterRequest;
import com.skillswap.dto.request.skill.SkillRequest;
import com.skillswap.dto.request.skill.SkillSearchRequest;
import com.skillswap.dto.request.skill.UserSkillRequest;
import com.skillswap.dto.response.skill.CategoryResponse;
import com.skillswap.dto.response.skill.SkillResponse;
import com.skillswap.dto.response.skill.UserSkillResponse;
import org.springframework.data.domain.Page;

/**
 * Application service for skill marketplace use cases.
 */
public interface SkillService {

    /**
     * Lists categories.
     *
     * @return all categories
     */
    java.util.List<CategoryResponse> getCategories();

    /**
     * Gets a category by id.
     *
     * @param id category id
     * @return category
     */
    CategoryResponse getCategory(Long id);

    /**
     * Creates a category.
     *
     * @param request category request
     * @return created category
     */
    CategoryResponse createCategory(CategoryRequest request);

    /**
     * Updates a category.
     *
     * @param id category id
     * @param request category request
     * @return updated category
     */
    CategoryResponse updateCategory(Long id, CategoryRequest request);

    /**
     * Deletes a category.
     *
     * @param id category id
     */
    void deleteCategory(Long id);

    /**
     * Lists skills with pagination and optional name search.
     *
     * @param search search term
     * @param page page number
     * @param size page size
     * @param sortBy sort property
     * @param sortDirection sort direction
     * @return paged skills
     */
    Page<SkillResponse> getSkills(String search, Integer page, Integer size, String sortBy, String sortDirection);

    /**
     * Gets a skill by id.
     *
     * @param id skill id
     * @return skill
     */
    SkillResponse getSkill(Long id);

    /**
     * Creates a skill.
     *
     * @param request skill request
     * @return created skill
     */
    SkillResponse createSkill(SkillRequest request);

    /**
     * Updates a skill.
     *
     * @param id skill id
     * @param request skill request
     * @return updated skill
     */
    SkillResponse updateSkill(Long id, SkillRequest request);

    /**
     * Deletes a skill.
     *
     * @param id skill id
     */
    void deleteSkill(Long id);

    /**
     * Searches user skill listings from query parameters.
     *
     * @param request search request
     * @return matching listings
     */
    Page<UserSkillResponse> searchListings(SkillSearchRequest request);

    /**
     * Searches user skill listings from an advanced filter body.
     *
     * @param request filter request
     * @return matching listings
     */
    Page<UserSkillResponse> filterListings(SkillFilterRequest request);

    /**
     * Adds a skill to the authenticated user's profile.
     *
     * @param request user skill request
     * @return saved listing
     */
    UserSkillResponse addUserSkill(UserSkillRequest request);

    /**
     * Lists skills for the authenticated user.
     *
     * @param page page number
     * @param size page size
     * @param sortBy sort property
     * @param sortDirection sort direction
     * @return paged user skills
     */
    Page<UserSkillResponse> getCurrentUserSkills(Integer page, Integer size, String sortBy, String sortDirection);

    /**
     * Deletes a skill listing owned by the authenticated user.
     *
     * @param userSkillId listing id
     */
    void deleteCurrentUserSkill(Long userSkillId);

    /**
     * Returns basic marketplace recommendations.
     *
     * @param page page number
     * @param size page size
     * @return recommended listings
     */
    Page<UserSkillResponse> getRecommendations(Integer page, Integer size);
}
