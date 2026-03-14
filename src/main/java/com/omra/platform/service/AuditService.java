package com.omra.platform.service;

import com.omra.platform.entity.AuditLog;
import com.omra.platform.repository.AuditLogRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(String action, String entity, String entityId) {
        try {
            AuditLog log = AuditLog.builder()
                    .userId(TenantContext.getUserId())
                    .agencyId(TenantContext.getAgencyId())
                    .action(action)
                    .entity(entity)
                    .entityId(entityId)
                    .timestamp(Instant.now())
                    .build();
            auditLogRepository.save(log);
        } catch (Exception ignored) {
            // Do not fail request on audit failure
        }
    }
}
