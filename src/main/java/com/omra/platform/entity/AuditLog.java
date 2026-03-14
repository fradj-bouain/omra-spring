package com.omra.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_agency_id", columnList = "agency_id"),
    @Index(name = "idx_audit_entity", columnList = "entity, entity_id"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "agency_id")
    private Long agencyId;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String entity;

    private String entityId;

    @Column(nullable = false)
    private Instant timestamp;
}
