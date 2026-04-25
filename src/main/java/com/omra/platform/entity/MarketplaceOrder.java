package com.omra.platform.entity;

import com.omra.platform.entity.enums.MarketplaceOrderStatus;
import com.omra.platform.entity.enums.MarketplacePaymentMode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "marketplace_orders", indexes = {
        @Index(name = "idx_mp_order_agency_id", columnList = "agency_id"),
        @Index(name = "idx_mp_order_pilgrim_id", columnList = "pilgrim_id"),
        @Index(name = "idx_mp_order_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketplaceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Agency that will fulfill/ship the order (expediteur). */
    @Column(name = "agency_id", nullable = false)
    private Long agencyId;

    /** Pilgrim (traveler) customer in the system. */
    @Column(name = "pilgrim_id", nullable = false)
    private Long pilgrimId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MarketplaceOrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false, length = 16)
    private MarketplacePaymentMode paymentMode;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal total;

    @Column(nullable = false, length = 8)
    private String currency;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = MarketplaceOrderStatus.PENDING;
        if (paymentMode == null) paymentMode = MarketplacePaymentMode.CASH;
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}

