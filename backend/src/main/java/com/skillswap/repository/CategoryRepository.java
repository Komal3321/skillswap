package com.skillswap.repository;

import java.util.Optional;

import com.skillswap.domain.entity.Category;

/**
 * Repository for skill categories.
 */
public interface CategoryRepository extends BaseRepository<Category, Long> {

    /**
     * Checks whether a category name already exists.
     *
     * @param name category name
     * @return true when present
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Finds a category by case-insensitive name.
     *
     * @param name category name
     * @return category when present
     */
    Optional<Category> findByNameIgnoreCase(String name);
}
