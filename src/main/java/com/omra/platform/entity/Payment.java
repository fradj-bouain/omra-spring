package com.omra.platform.entity;

import com.omra.platform.entity.enums.PaymentMethod;
import com.omra.platform.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Paiement lié à un pèlerin et un groupe (obligatoires).
 * En cas de statut PARTIAL : première date d'échéance + période + nombre d'échéances → génération des échéances (PaymentDue).
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_agency_id", columnList = "agency_id"),
    @Index(name = "idx_payment_pilgrim_id", columnList = "pilgrim_id"),
    @Index(name = "idx_payment_group_id", columnList = "group_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agency_id", nullable = false)
    private Long agencyId;

    @Column(name = "pilgrim_id")
    private Long pilgrimId;

    @Column(name = "group_id")
    private Long groupId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private LocalDate paymentDate;
    private String reference;

    @Column(name = "first_due_date")
    private LocalDate firstDueDate;

    @Column(name = "due_period_days")
    private Integer duePeriodDays;

    @Column(name = "number_of_installments")
    private Integer numberOfInstallments;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
