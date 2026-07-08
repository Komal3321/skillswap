package com.skillswap.domain.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.skillswap.domain.common.BaseEntity;
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
    @Column(nullable = false)
    private Instant startTime;

    @NotNull
    @Column(nullable = false)
    private Instant endTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SessionStatus status;

    @Size(max = 255)
    @Column(length = 255)
    private String meetingUrl;

    @Size(max = 1000)
    @Column(length = 1000)
    private String notes;

    @Builder.Default
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private Set<Review> reviews = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private Set<Certificate> certificates = new HashSet<>();

    public enum SessionStatus {
        SCHEDULED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED,
        NO_SHOW
    }
}
