package com.omra.platform.repository;

import com.omra.platform.entity.Payment;
import com.omra.platform.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Page<Payment> findByAgencyIdAndDeletedAtIsNull(Long agencyId, Pageable pageable);

    List<Payment> findByPilgrimIdAndDeletedAtIsNull(Long pilgrimId);

    List<Payment> findByGroupIdAndDeletedAtIsNull(Long groupId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.agencyId = :agencyId AND p.status = :status AND p.deletedAt IS NULL")
    BigDecimal sumAmountByAgencyIdAndStatus(@Param("agencyId") Long agencyId, @Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.groupId = :groupId AND p.status = :status AND p.deletedAt IS NULL")
    BigDecimal sumAmountByGroupIdAndStatus(@Param("groupId") Long groupId, @Param("status") PaymentStatus status);

    List<Payment> findByAgencyIdAndStatusAndDeletedAtIsNullAndPaymentDateBetween(Long agencyId, PaymentStatus status, LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status AND p.deletedAt IS NULL")
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);

    Page<Payment> findByDeletedAtIsNull(Pageable pageable);

    List<Payment> findByStatusAndDeletedAtIsNullAndPaymentDateBetween(PaymentStatus status, LocalDate start, LocalDate end);
}
