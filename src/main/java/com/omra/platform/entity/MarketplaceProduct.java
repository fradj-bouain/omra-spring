package com.omra.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Product in a marketplace. For MANUAL marketplaces, products are stored here.
 * For EXTERNAL_API marketplaces, this table can be used later for caching/sync; currently optional.
 */
@Entity
@Table(name = "marketplace_products", indexes = {
        @Index(name = "idx_mp_product_agency_id", columnList = "agency_id"),
        @Index(name = "idx_mp_product_marketplace_id", columnList = "marketplace_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketplaceProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agency_id", nullable = false)
    private Long agencyId;

    @Column(name = "marketplace_id", nullable = false)
    private Long marketplaceId;

    /** Optional external id (if provided by external marketplace). */
    @Column(name = "external_id", length = 128)
    private String externalId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(nullable = false, length = 8)
    private String currency;

    @Column(name = "in_stock", nullable = false)
    private boolean inStock;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}

