package com.omra.platform.repository;

import com.omra.platform.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<AuditLog> findByAgencyIdOrderByCreatedAtDesc(Long agencyId, Pageable pageable);

    Page<AuditLog> findByUserIdAndActionTypeOrderByCreatedAtDesc(Long userId, String actionType, Pageable pageable);

    Page<AuditLog> findByUserIdAndEntityTypeOrderByCreatedAtDesc(Long userId, String entityType, Pageable pageable);

    Page<AuditLog> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long userId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<AuditLog> findByAgencyIdAndActionTypeOrderByCreatedAtDesc(Long agencyId, String actionType, Pageable pageable);

    Page<AuditLog> findByAgencyIdAndEntityTypeOrderByCreatedAtDesc(Long agencyId, String entityType, Pageable pageable);

    Page<AuditLog> findByAgencyIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long agencyId, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
