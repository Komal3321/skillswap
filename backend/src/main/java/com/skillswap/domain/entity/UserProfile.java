package com.skillswap.domain.entity;

import java.util.HashSet;
import java.util.Set;

import com.skillswap.domain.common.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@Table(name = "user_profiles")
public class UserProfile extends BaseEntity {

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Size(max = 120)
    @Column(length = 120)
    private String city;

    @Size(max = 120)
    @Column(length = 120)
    private String country;

    @Size(max = 80)
    @Column(length = 80)
    private String timeZone;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private LearningMode preferredLearningMode;

    @Size(max = 1000)
    @Column(length = 1000)
    private String experience;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "user_profile_languages", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "language", length = 80, nullable = false)
    private Set<String> languages = new HashSet<>();

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "user_profile_portfolio_links", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "url", length = 500, nullable = false)
    private Set<String> portfolioLinks = new HashSet<>();

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "user_profile_portfolio_documents", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "document_url", length = 500, nullable = false)
    private Set<String> portfolioDocumentUrls = new HashSet<>();

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "user_profile_certificate_documents", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "certificate_url", length = 500, nullable = false)
    private Set<String> certificateUrls = new HashSet<>();

    public enum LearningMode {
        ONLINE,
        OFFLINE,
        BOTH
    }
}
