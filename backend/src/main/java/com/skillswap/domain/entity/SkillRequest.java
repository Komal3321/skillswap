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
@Table(name = "skill_requests")
public class SkillRequest extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Size(max = 1000)
    @Column(length = 1000)
    private String message;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RequestStatus status;

    private Instant requestedStartTime;

    private Instant requestedEndTime;

    @Builder.Default
    @OneToMany(mappedBy = "skillRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Session> sessions = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "skillRequest", cascade = CascadeType.ALL)
    private Set<Message> messages = new HashSet<>();

    public enum RequestStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        CANCELLED,
        COMPLETED
    }
}
