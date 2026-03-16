package com.omra.platform.service;

import com.omra.platform.entity.AuditLog;
import com.omra.platform.entity.enums.UserRole;
import com.omra.platform.repository.AdminRepository;
import com.omra.platform.repository.AuditLogRepository;
import com.omra.platform.repository.UserRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private static final int MAX_BODY_LENGTH = 10_000;
    private static final String MASK = "***MASKED***";
    private static final Pattern SENSITIVE_KEYS = Pattern.compile(
            "(?i)(password|token|secret|authorization|bearer|jwt|refresh_token|api_key|credential)"
    );
    private static final Pattern SENSITIVE_VALUE = Pattern.compile(
            "(?i)(bearer\\s+[\\w.-]+|basic\\s+[a-zA-Z0-9+=/]+)"
    );

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    @Async
    public void saveFromRequest(
            String apiEndpoint,
            String httpMethod,
            String requestBody,
            String responseBody,
            int statusCode,
            String ipAddress) {
        try {
            Long userId = TenantContext.getUserId();
            Long adminId = TenantContext.getAdminId();
            Long agencyId = TenantContext.getAgencyId();
            String userEmail = resolveUserEmail(userId, adminId);

            String actionType = inferActionType(httpMethod, apiEndpoint, requestBody);
            String entityType = inferEntityType(apiEndpoint);
            Long entityId = parseEntityIdFromPath(apiEndpoint);

            String safeRequest = maskSensitiveData(truncate(requestBody, MAX_BODY_LENGTH));
            String safeResponse = maskSensitiveData(truncate(responseBody, MAX_BODY_LENGTH));

            AuditLog auditLog = AuditLog.builder()
                    .agencyId(agencyId)
                    .userId(adminId != null ? adminId : userId)
                    .userEmail(userEmail)
                    .actionType(actionType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .apiEndpoint(apiEndpoint)
                    .httpMethod(httpMethod)
                    .requestData(safeRequest)
                    .responseData(safeResponse)
                    .statusCode(statusCode)
                    .ipAddress(ipAddress)
                    .createdAt(LocalDateTime.now())
                    .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.warn("Audit log save failed: {}", e.getMessage());
        }
    }

    public Page<AuditLog> findFiltered(Long userId, String actionType, String entityType,
                                        LocalDateTime startDate, LocalDateTime endDate,
                                        Long agencyIdFilter, Pageable pageable) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (actionType != null && !actionType.isBlank()) {
                predicates.add(cb.equal(root.get("actionType"), actionType));
            }
            if (entityType != null && !entityType.isBlank()) {
                predicates.add(cb.equal(root.get("entityType"), entityType));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }
            if (agencyIdFilter != null) {
                predicates.add(cb.equal(root.get("agencyId"), agencyIdFilter));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return auditLogRepository.findAll(spec, pageable);
    }

    public Optional<AuditLog> findById(Long id) {
        return auditLogRepository.findById(id);
    }

    /** Programmatic audit (e.g. from existing AuditService). */
    @Async
    public void logAction(String actionType, String entityType, Long entityId) {
        try {
            Long userId = TenantContext.getUserId();
            Long adminId = TenantContext.getAdminId();
            Long agencyId = TenantContext.getAgencyId();
            String userEmail = resolveUserEmail(userId, adminId);
            AuditLog auditLog = AuditLog.builder()
                    .agencyId(agencyId)
                    .userId(adminId != null ? adminId : userId)
                    .userEmail(userEmail)
                    .actionType(actionType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .createdAt(LocalDateTime.now())
                    .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.warn("Audit logAction failed: {}", e.getMessage());
        }
    }

    String resolveUserEmail(Long userId, Long adminId) {
        if (adminId != null) {
            return adminRepository.findById(adminId).map(a -> a.getEmail()).orElse(null);
        }
        if (userId != null) {
            return userRepository.findById(userId).map(u -> u.getEmail()).orElse(null);
        }
        return null;
    }

    static String inferActionType(String method, String path, String requestBody) {
        String lower = path.toLowerCase();
        if (lower.contains("/auth/login") || lower.contains("/auth/refresh")) return "LOGIN";
        if (lower.contains("/upload") || lower.contains("/import")) return "UPLOAD";
        if (lower.contains("/payment") && ("POST".equals(method))) return "PAYMENT_CREATE";
        if (lower.contains("/group") && lower.contains("assign")) return "GROUP_ASSIGN";
        switch (method) {
            case "POST": return "CREATE";
            case "PUT":
            case "PATCH": return "UPDATE";
            case "DELETE": return "DELETE";
            default: return method;
        }
    }

    static String inferEntityType(String path) {
        if (path == null || path.isEmpty()) return null;
        String[] segments = path.replaceFirst("^/api/", "").split("/");
        if (segments.length > 0 && !segments[0].isEmpty()) {
            String first = segments[0];
            if (first.matches("^[0-9]+$")) return segments.length > 1 ? segments[1] : first;
            return first;
        }
        return null;
    }

    static Long parseEntityIdFromPath(String path) {
        if (path == null) return null;
        String[] parts = path.split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (parts[i].matches("^[0-9]+$")) {
                try {
                    return Long.parseLong(parts[i]);
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    static String maskSensitiveData(String raw) {
        if (raw == null || raw.isEmpty()) return raw;
        String s = raw;
        s = SENSITIVE_VALUE.matcher(s).replaceAll(MASK);
        if (s.contains("\"password\"") || s.contains("\"token\"") || s.contains("\"authorization\"")) {
            s = s.replaceAll("(\"(?:password|token|authorization|refreshToken|apiKey)\"\\s*:\\s*)\"[^\"]*\"", "$1\"" + MASK + "\"");
        }
        return s;
    }

    static String truncate(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, max) + "...[truncated]";
    }
}
