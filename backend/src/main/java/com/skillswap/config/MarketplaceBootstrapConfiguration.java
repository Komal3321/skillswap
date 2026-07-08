package com.skillswap.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.skillswap.domain.entity.Category;
import com.skillswap.domain.entity.Skill;
import com.skillswap.repository.CategoryRepository;
import com.skillswap.repository.SkillRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Seeds the default marketplace category and skill catalog.
 */
@Configuration
public class MarketplaceBootstrapConfiguration {

    /**
     * Inserts missing default categories and skills at startup.
     *
     * @param categoryRepository category repository
     * @param skillRepository skill repository
     * @return startup runner
     */
    @Bean
    public ApplicationRunner marketplaceBootstrapRunner(
            CategoryRepository categoryRepository,
            SkillRepository skillRepository) {
        return args -> defaultCatalog().forEach((categoryName, skills) -> {
            Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                    .orElseGet(() -> categoryRepository.save(Category.builder()
                            .name(categoryName)
                            .description(categoryName + " skills")
                            .build()));
            skills.stream()
                    .filter(skillName -> skillRepository.findByNameIgnoreCase(skillName).isEmpty())
                    .map(skillName -> Skill.builder()
                            .name(skillName)
                            .category(category)
                            .description(skillName + " learning and mentoring")
                            .build())
                    .forEach(skillRepository::save);
        });
    }

    private Map<String, List<String>> defaultCatalog() {
        Map<String, List<String>> catalog = new LinkedHashMap<>();
        catalog.put("Programming", List.of("Java", "Spring Boot", "Python", "React", "Node.js", "AWS"));
        catalog.put("Creative", List.of("Photoshop", "UI Design", "Graphic Design", "Video Editing"));
        catalog.put("Music", List.of("Guitar", "Piano", "Singing"));
        catalog.put("Languages", List.of("English", "Hindi", "Japanese"));
        catalog.put("Business", List.of("Marketing", "Sales", "Finance"));
        catalog.put("Fitness", List.of("Yoga", "Gym", "Dance"));
        return catalog;
    }
}
