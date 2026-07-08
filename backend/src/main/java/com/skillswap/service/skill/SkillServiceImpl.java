package com.skillswap.service.skill;

import java.util.List;
import java.util.Map;

import com.skillswap.common.exception.BadRequestException;
import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.domain.entity.Availability.AvailabilityMode;
import com.skillswap.domain.entity.Category;
import com.skillswap.domain.entity.Skill;
import com.skillswap.domain.entity.User;
import com.skillswap.domain.entity.UserProfile;
import com.skillswap.domain.entity.UserSkill;
import com.skillswap.domain.entity.UserSkill.SkillType;
import com.skillswap.dto.request.skill.CategoryRequest;
import com.skillswap.dto.request.skill.SkillFilterRequest;
import com.skillswap.dto.request.skill.SkillRequest;
import com.skillswap.dto.request.skill.SkillSearchRequest;
import com.skillswap.dto.request.skill.UserSkillRequest;
import com.skillswap.dto.response.skill.CategoryResponse;
import com.skillswap.dto.response.skill.SkillResponse;
import com.skillswap.dto.response.skill.UserSkillResponse;
import com.skillswap.repository.CategoryRepository;
import com.skillswap.repository.SkillRepository;
import com.skillswap.repository.UserProfileRepository;
import com.skillswap.repository.UserRepository;
import com.skillswap.repository.UserSkillRepository;
import com.skillswap.security.user.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default skill marketplace service implementation.
 */
@Service
public class SkillServiceImpl implements SkillService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final Map<String, String> SKILL_SORT_FIELDS = Map.of(
            "id", "id",
            "name", "name",
            "category", "category.name",
            "createdAt", "createdAt");
    private static final Map<String, String> USER_SKILL_SORT_FIELDS = Map.of(
            "id", "id",
            "skillName", "skill.name",
            "category", "skill.category.name",
            "experience", "yearsOfExperience",
            "createdAt", "createdAt");

    private final CategoryRepository categoryRepository;
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public SkillServiceImpl(
            CategoryRepository categoryRepository,
            SkillRepository skillRepository,
            UserSkillRepository userSkillRepository,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository) {
        this.categoryRepository = categoryRepository;
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long id) {
        return toCategoryResponse(findCategory(id));
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String name = normalizeRequired(request.name(), "Category name");
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Category already exists");
        }
        Category category = Category.builder()
                .name(name)
                .description(normalizeNullable(request.description()))
                .build();
        return toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findCategory(id);
        String name = normalizeRequired(request.name(), "Category name");
        categoryRepository.findByNameIgnoreCase(name)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BadRequestException("Category already exists");
                });
        category.setName(name);
        category.setDescription(normalizeNullable(request.description()));
        return toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = findCategory(id);
        if (!category.getSkills().isEmpty()) {
            throw new BadRequestException("Category cannot be deleted while skills are assigned");
        }
        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SkillResponse> getSkills(String search, Integer page, Integer size, String sortBy, String sortDirection) {
        Pageable pageable = pageRequest(page, size, sortBy, sortDirection, SKILL_SORT_FIELDS, "name");
        String normalizedSearch = normalizeNullable(search);
        Page<Skill> skills = normalizedSearch == null
                ? skillRepository.findAll(pageable)
                : skillRepository.findByNameContainingIgnoreCase(normalizedSearch, pageable);
        return skills.map(this::toSkillResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SkillResponse getSkill(Long id) {
        return toSkillResponse(findSkill(id));
    }

    @Override
    @Transactional
    public SkillResponse createSkill(SkillRequest request) {
        String name = normalizeRequired(request.name(), "Skill name");
        if (skillRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Skill already exists");
        }
        Skill skill = Skill.builder()
                .name(name)
                .description(normalizeNullable(request.description()))
                .category(findCategory(request.categoryId()))
                .build();
        return toSkillResponse(skillRepository.save(skill));
    }

    @Override
    @Transactional
    public SkillResponse updateSkill(Long id, SkillRequest request) {
        Skill skill = findSkill(id);
        String name = normalizeRequired(request.name(), "Skill name");
        skillRepository.findByNameIgnoreCase(name)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BadRequestException("Skill already exists");
                });
        skill.setName(name);
        skill.setDescription(normalizeNullable(request.description()));
        skill.setCategory(findCategory(request.categoryId()));
        return toSkillResponse(skillRepository.save(skill));
    }

    @Override
    @Transactional
    public void deleteSkill(Long id) {
        skillRepository.delete(findSkill(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserSkillResponse> searchListings(SkillSearchRequest request) {
        return searchUserSkills(
                request.skillName(),
                request.categoryId(),
                request.city(),
                request.language(),
                request.experienceLevel(),
                request.minRating(),
                request.availabilityMode(),
                request.skillType(),
                request.page(),
                request.size(),
                request.sortBy(),
                request.sortDirection());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserSkillResponse> filterListings(SkillFilterRequest request) {
        return searchUserSkills(
                request.skillName(),
                request.categoryId(),
                request.city(),
                request.language(),
                request.experienceLevel(),
                request.minRating(),
                request.availabilityMode(),
                request.skillType(),
                request.page(),
                request.size(),
                request.sortBy(),
                request.sortDirection());
    }

    @Override
    @Transactional
    public UserSkillResponse addUserSkill(UserSkillRequest request) {
        User user = findUser(currentUserId());
        Skill skill = findSkill(request.skillId());
        boolean duplicate = userSkillRepository.findByUserId(user.getId())
                .stream()
                .anyMatch(existing -> existing.getSkill().getId().equals(skill.getId())
                        && existing.getSkillType() == request.skillType());
        if (duplicate) {
            throw new BadRequestException("Skill is already listed on this profile");
        }

        UserSkill userSkill = UserSkill.builder()
                .user(user)
                .skill(skill)
                .skillType(request.skillType())
                .proficiencyLevel(request.proficiencyLevel())
                .yearsOfExperience(request.yearsOfExperience())
                .description(normalizeNullable(request.description()))
                .verified(false)
                .build();
        return toUserSkillResponse(userSkillRepository.save(userSkill));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserSkillResponse> getCurrentUserSkills(Integer page, Integer size, String sortBy, String sortDirection) {
        Pageable pageable = pageRequest(page, size, sortBy, sortDirection, USER_SKILL_SORT_FIELDS, "createdAt");
        return userSkillRepository.findByUserId(currentUserId(), pageable).map(this::toUserSkillResponse);
    }

    @Override
    @Transactional
    public void deleteCurrentUserSkill(Long userSkillId) {
        UserSkill userSkill = userSkillRepository.findById(userSkillId)
                .orElseThrow(() -> new ResourceNotFoundException("User skill not found"));
        if (!userSkill.getUser().getId().equals(currentUserId())) {
            throw new BadRequestException("Cannot delete another user's skill listing");
        }
        userSkillRepository.delete(userSkill);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserSkillResponse> getRecommendations(Integer page, Integer size) {
        Pageable pageable = pageRequest(page, size, "createdAt", "desc", USER_SKILL_SORT_FIELDS, "createdAt");
        return userSkillRepository.searchListings(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                AvailabilityMode.BOTH,
                SkillType.OFFERED,
                pageable).map(this::toUserSkillResponse);
    }

    private Page<UserSkillResponse> searchUserSkills(
            String skillName,
            Long categoryId,
            String city,
            String language,
            UserSkill.ProficiencyLevel experienceLevel,
            Double minRating,
            AvailabilityMode availabilityMode,
            SkillType skillType,
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection) {
        Pageable pageable = pageRequest(page, size, sortBy, sortDirection, USER_SKILL_SORT_FIELDS, "createdAt");
        return userSkillRepository.searchListings(
                normalizeNullable(skillName),
                categoryId,
                normalizeNullable(city),
                normalizeNullable(language),
                experienceLevel,
                minRating,
                availabilityMode,
                AvailabilityMode.BOTH,
                skillType,
                pageable).map(this::toUserSkillResponse);
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private Skill findSkill(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getSkills().size());
    }

    private SkillResponse toSkillResponse(Skill skill) {
        return new SkillResponse(
                skill.getId(),
                skill.getName(),
                skill.getDescription(),
                skill.getCategory().getId(),
                skill.getCategory().getName());
    }

    private UserSkillResponse toUserSkillResponse(UserSkill userSkill) {
        User user = userSkill.getUser();
        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
        Skill skill = userSkill.getSkill();
        return new UserSkillResponse(
                userSkill.getId(),
                user.getId(),
                user.getFullName(),
                profile == null ? null : profile.getCity(),
                skill.getId(),
                skill.getName(),
                skill.getCategory().getId(),
                skill.getCategory().getName(),
                userSkill.getSkillType(),
                userSkill.getProficiencyLevel(),
                userSkill.getYearsOfExperience(),
                userSkill.getDescription(),
                userSkill.isVerified(),
                userSkillRepository.averageRatingForUser(user.getId()));
    }

    private Pageable pageRequest(
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection,
            Map<String, String> allowedSortFields,
            String defaultSort) {
        int resolvedPage = page == null ? DEFAULT_PAGE : page;
        int resolvedSize = size == null ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        String requestedSort = normalizeNullable(sortBy);
        String sortProperty = allowedSortFields.getOrDefault(requestedSort, allowedSortFields.get(defaultSort));
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(resolvedPage, resolvedSize, Sort.by(direction, sortProperty));
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            throw new BadRequestException("Authenticated user is required");
        }
        return principal.getId();
    }

    private String normalizeRequired(String value, String label) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            throw new BadRequestException(label + " is required");
        }
        return normalized;
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
