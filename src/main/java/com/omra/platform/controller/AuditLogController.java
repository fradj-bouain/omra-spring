package com.omra.platform.controller;

import com.omra.platform.dto.AuditLogResponse;
import com.omra.platform.dto.PageResponse;
import com.omra.platform.entity.AuditLog;
import com.omra.platform.entity.enums.UserRole;
import com.omra.platform.service.AuditLogService;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<PageResponse<AuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long agencyId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long effectiveAgencyId = null;
        if (UserRole.SUPER_ADMIN != TenantContext.getUserRole()) {
            effectiveAgencyId = TenantContext.getAgencyId();
        } else if (agencyId != null) {
            effectiveAgencyId = agencyId;
        }

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.min(100, Math.max(1, size)));
        Page<AuditLog> result = auditLogService.findFiltered(
                userId, actionType, entityType, startDate, endDate, effectiveAgencyId, pageable);

        List<AuditLogResponse> content = result.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        PageResponse<AuditLogResponse> body = PageResponse.<AuditLogResponse>builder()
                .content(content)
                .page(result.getNumber() + 1)
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .first(result.isFirst())
                .last(result.isLast())
                .build();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLogResponse> getAuditLogById(@PathVariable Long id) {
        return auditLogService.findById(id)
                .filter(log -> TenantContext.getUserRole() == UserRole.SUPER_ADMIN
                        || (log.getAgencyId() != null && log.getAgencyId().equals(TenantContext.getAgencyId())))
                .map(this::toDetailResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .userEmail(log.getUserEmail())
                .actionType(log.getActionType())
                .entityType(log.getEntityType())
                .apiEndpoint(log.getApiEndpoint())
                .httpMethod(log.getHttpMethod())
                .statusCode(log.getStatusCode())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private AuditLogResponse toDetailResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .userEmail(log.getUserEmail())
                .actionType(log.getActionType())
                .entityType(log.getEntityType())
                .apiEndpoint(log.getApiEndpoint())
                .httpMethod(log.getHttpMethod())
                .statusCode(log.getStatusCode())
                .createdAt(log.getCreatedAt())
                .requestData(log.getRequestData())
                .responseData(log.getResponseData())
                .ipAddress(log.getIpAddress())
                .entityId(log.getEntityId())
                .userId(log.getUserId())
                .agencyId(log.getAgencyId())
                .build();
    }
}
