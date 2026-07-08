package com.skillswap.domain.entity;

import com.skillswap.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "user_skills",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "skill_id", "skill_type"}))
public class UserSkill extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "skill_type", nullable = false, length = 30)
    private SkillType skillType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProficiencyLevel proficiencyLevel;

    @Min(0)
    @Max(80)
    @Column(nullable = false)
    private Integer yearsOfExperience;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private boolean verified = false;

    public enum SkillType {
        OFFERED,
        WANTED
    }

    public enum ProficiencyLevel {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED,
        EXPERT
    }
}
