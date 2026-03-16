package com.omra.platform.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {

    private Long id;
    private String userEmail;
    private String actionType;
    private String entityType;
    private String apiEndpoint;
    private String httpMethod;
    private Integer statusCode;
    private LocalDateTime createdAt;

    /** Optional: include for detail view only */
    private String requestData;
    private String responseData;
    private String ipAddress;
    private Long entityId;
    private Long userId;
    private Long agencyId;
}
