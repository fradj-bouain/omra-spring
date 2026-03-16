package com.omra.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_agency_id", columnList = "agency_id"),
    @Index(name = "idx_audit_entity_type_id", columnList = "entity_type, referenced_entity_id"),
    @Index(name = "idx_audit_created_at", columnList = "created_at"),
    @Index(name = "idx_audit_action_type", columnList = "action_type")
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

    @Column(name = "agency_id")
    private Long agencyId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(name = "action_type", length = 64)
    private String actionType;

    @Column(name = "entity_type", length = 128)
    private String entityType;

    @Column(name = "referenced_entity_id")
    private Long entityId;

    @Column(name = "api_endpoint", length = 512)
    private String apiEndpoint;

    @Column(name = "http_method", length = 16)
    private String httpMethod;

    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData;

    @Column(name = "response_data", columnDefinition = "TEXT")
    private String responseData;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
