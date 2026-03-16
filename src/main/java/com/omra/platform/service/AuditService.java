package com.omra.platform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Legacy programmatic audit. Prefer automatic request logging via AuditLogInterceptor.
 * This service delegates to AuditLogService for persistence.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogService auditLogService;

    /** @deprecated Prefer automatic API audit. Use AuditLogService.logAction for programmatic logs. */
    @Deprecated
    public void log(String action, String entity, String entityId) {
        Long id = null;
        if (entityId != null && !entityId.isBlank()) {
            try {
                id = Long.parseLong(entityId);
            } catch (NumberFormatException ignored) {}
        }
        auditLogService.logAction(action, entity, id);
    }
}
