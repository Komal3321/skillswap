package com.skillswap.controller.skill;

import com.skillswap.common.api.ApiResponse;
import com.skillswap.domain.entity.Availability.AvailabilityMode;
import com.skillswap.domain.entity.UserSkill.ProficiencyLevel;
import com.skillswap.domain.entity.UserSkill.SkillType;
import com.skillswap.dto.request.skill.SkillFilterRequest;
import com.skillswap.dto.request.skill.SkillRequest;
import com.skillswap.dto.request.skill.SkillSearchRequest;
import com.skillswap.dto.request.skill.UserSkillRequest;
import com.skillswap.dto.response.skill.SkillResponse;
import com.skillswap.dto.response.skill.UserSkillResponse;
import com.skillswap.service.skill.SkillService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for marketplace skill and user skill APIs.
 */
@Validated
@RestController
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    /**
     * Lists skills with pagination, sorting, and optional name search.
     *
     * @param search skill name fragment
     * @param page page number
     * @param size page size
     * @param sortBy sort property
     * @param sortDirection sort direction
     * @return paged skills
     */
    @GetMapping("/api/skills")
    public ResponseEntity<ApiResponse<Page<SkillResponse>>> getSkills(
            @RequestParam(required = false) @Size(max = 120) String search,
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size,
            @RequestParam(required = false) @Size(max = 40) String sortBy,
            @RequestParam(required = false) @Size(max = 4) String sortDirection) {
        return ResponseEntity.ok(ApiResponse.success("Skills fetched successfully",
                skillService.getSkills(search, page, size, sortBy, sortDirection)));
    }

    /**
     * Searches user skill listings.
     *
     * @return paged matching listings
     */
    @GetMapping("/api/skills/search")
    public ResponseEntity<ApiResponse<Page<UserSkillResponse>>> searchSkills(
            @RequestParam(required = false) @Size(max = 120) String skillName,
            @RequestParam(required = false) @Positive Long categoryId,
            @RequestParam(required = false) @Size(max = 120) String city,
            @RequestParam(required = false) @Size(max = 80) String language,
            @RequestParam(required = false) ProficiencyLevel experienceLevel,
            @RequestParam(required = false) @Min(1) @Max(5) Double minRating,
            @RequestParam(required = false) AvailabilityMode availabilityMode,
            @RequestParam(required = false) SkillType skillType,
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size,
            @RequestParam(required = false) @Size(max = 40) String sortBy,
            @RequestParam(required = false) @Size(max = 4) String sortDirection) {
        SkillSearchRequest request = new SkillSearchRequest(
                skillName,
                categoryId,
                city,
                language,
                experienceLevel,
                minRating,
                availabilityMode,
                skillType,
                page,
                size,
                sortBy,
                sortDirection);
        return ResponseEntity.ok(ApiResponse.success("Skill listings fetched successfully",
                skillService.searchListings(request)));
    }

    /**
     * Runs advanced listing filters.
     *
     * @param request filter request
     * @return paged matching listings
     */
    @PostMapping("/api/skills/filter")
    public ResponseEntity<ApiResponse<Page<UserSkillResponse>>> filterSkills(
            @Valid @RequestBody SkillFilterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Skill listings fetched successfully",
                skillService.filterListings(request)));
    }

    /**
     * Returns basic skill recommendations.
     *
     * @param page page number
     * @param size page size
     * @return recommended listings
     */
    @GetMapping("/api/skills/recommendations")
    public ResponseEntity<ApiResponse<Page<UserSkillResponse>>> getRecommendations(
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size) {
        return ResponseEntity.ok(ApiResponse.success("Skill recommendations fetched successfully",
                skillService.getRecommendations(page, size)));
    }

    /**
     * Gets a skill by id.
     *
     * @param id skill id
     * @return skill
     */
    @GetMapping("/api/skills/{id}")
    public ResponseEntity<ApiResponse<SkillResponse>> getSkill(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Skill fetched successfully", skillService.getSkill(id)));
    }

    /**
     * Creates a skill. Requires administrator access.
     *
     * @param request skill request
     * @return created skill
     */
    @PostMapping("/api/skills")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SkillResponse>> createSkill(@Valid @RequestBody SkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Skill created successfully", skillService.createSkill(request)));
    }

    /**
     * Updates a skill. Requires administrator access.
     *
     * @param id skill id
     * @param request skill request
     * @return updated skill
     */
    @PutMapping("/api/skills/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SkillResponse>> updateSkill(
            @PathVariable Long id,
            @Valid @RequestBody SkillRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Skill updated successfully",
                skillService.updateSkill(id, request)));
    }

    /**
     * Deletes a skill. Requires administrator access.
     *
     * @param id skill id
     * @return empty success response
     */
    @DeleteMapping("/api/skills/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSkill(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return ResponseEntity.ok(ApiResponse.success("Skill deleted successfully"));
    }

    /**
     * Adds a skill listing to the authenticated user's profile.
     *
     * @param request user skill request
     * @return saved user skill
     */
    @PostMapping("/api/user-skills")
    public ResponseEntity<ApiResponse<UserSkillResponse>> addUserSkill(
            @Valid @RequestBody UserSkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User skill added successfully", skillService.addUserSkill(request)));
    }

    /**
     * Lists skills owned by the authenticated user.
     *
     * @param page page number
     * @param size page size
     * @param sortBy sort property
     * @param sortDirection sort direction
     * @return paged user skills
     */
    @GetMapping("/api/user-skills")
    public ResponseEntity<ApiResponse<Page<UserSkillResponse>>> getUserSkills(
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size,
            @RequestParam(required = false) @Size(max = 40) String sortBy,
            @RequestParam(required = false) @Size(max = 4) String sortDirection) {
        return ResponseEntity.ok(ApiResponse.success("User skills fetched successfully",
                skillService.getCurrentUserSkills(page, size, sortBy, sortDirection)));
    }

    /**
     * Deletes an authenticated user's skill listing.
     *
     * @param id user skill id
     * @return empty success response
     */
    @DeleteMapping("/api/user-skills/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUserSkill(@PathVariable Long id) {
        skillService.deleteCurrentUserSkill(id);
        return ResponseEntity.ok(ApiResponse.success("User skill deleted successfully"));
    }
}
