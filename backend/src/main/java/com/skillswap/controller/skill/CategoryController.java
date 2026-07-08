package com.skillswap.controller.skill;

import java.util.List;

import com.skillswap.common.api.ApiResponse;
import com.skillswap.dto.request.skill.CategoryRequest;
import com.skillswap.dto.response.skill.CategoryResponse;
import com.skillswap.service.skill.SkillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for marketplace category APIs.
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final SkillService skillService;

    public CategoryController(SkillService skillService) {
        this.skillService = skillService;
    }

    /**
     * Lists all categories.
     *
     * @return categories
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success("Categories fetched successfully", skillService.getCategories()));
    }

    /**
     * Gets a category by id.
     *
     * @param id category id
     * @return category
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Category fetched successfully", skillService.getCategory(id)));
    }

    /**
     * Creates a category. Requires administrator access.
     *
     * @param request category request
     * @return created category
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", skillService.createCategory(request)));
    }

    /**
     * Updates a category. Requires administrator access.
     *
     * @param id category id
     * @param request category request
     * @return updated category
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully",
                skillService.updateCategory(id, request)));
    }

    /**
     * Deletes a category. Requires administrator access.
     *
     * @param id category id
     * @return empty success response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        skillService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully"));
    }
}
