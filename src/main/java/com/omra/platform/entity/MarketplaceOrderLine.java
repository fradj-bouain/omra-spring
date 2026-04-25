package com.omra.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "marketplace_order_lines", indexes = {
        @Index(name = "idx_mp_order_line_order_id", columnList = "order_id"),
        @Index(name = "idx_mp_order_line_marketplace_id", columnList = "marketplace_id"),
        @Index(name = "idx_mp_order_line_product_id", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketplaceOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "marketplace_id", nullable = false)
    private Long marketplaceId;

    /** If MANUAL product, this is an internal product id. */
    @Column(name = "product_id")
    private Long productId;

    /** If EXTERNAL_API product, keep the external id (string). */
    @Column(name = "external_product_id", length = 128)
    private String externalProductId;

    @Column(nullable = false, length = 220)
    private String title;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal lineTotal;

    @Column(nullable = false, length = 8)
    private String currency;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}

