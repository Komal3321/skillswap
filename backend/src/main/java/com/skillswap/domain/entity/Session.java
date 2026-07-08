package com.skillswap.domain.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.skillswap.domain.common.BaseEntity;
import com.skillswap.domain.enums.SessionStatus;
import com.skillswap.domain.enums.SessionType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "sessions")
public class Session extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_request_id", nullable = false)
    private SkillRequest skillRequest;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learner_id", nullable = false)
    private User learner;

    @NotNull
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String title;

    @Size(max = 1000)
    @Column(length = 1000)
    private String description;

    @NotNull
    @Column(nullable = false)
    private Instant startTime;

    @NotNull
    @Column(nullable = false)
    private Instant endTime;

    @NotNull
    @Column(nullable = false)
    private Integer durationMinutes;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SessionType meetingType;

    @Size(max = 500)
    @Column(length = 500)
    private String meetingLink;

    @Size(max = 255)
    @Column(length = 255)
    private String location;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SessionStatus status;

    @Size(max = 1000)
    @Column(length = 1000)
    private String notes;

    @Builder.Default
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private Set<Review> reviews = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private Set<Certificate> certificates = new HashSet<>();
}
