package com.skillswap.domain.entity;

import com.skillswap.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AuditAction action;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String entityName;

    @Size(max = 80)
    @Column(length = 80)
    private String entityId;

    @Size(max = 45)
    @Column(length = 45)
    private String ipAddress;

    @Size(max = 255)
    @Column(length = 255)
    private String userAgent;

    @Lob
    private String metadata;

    public enum AuditAction {
        CREATE,
        UPDATE,
        DELETE,
        LOGIN,
        LOGOUT,
        SECURITY_EVENT
    }
}
