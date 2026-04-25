package com.omra.platform.entity;

import com.omra.platform.entity.enums.MarketplaceCatalogType;
import com.omra.platform.entity.enums.MarketplaceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "marketplaces", indexes = {
        @Index(name = "idx_marketplace_agency_id", columnList = "agency_id"),
        @Index(name = "idx_marketplace_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Marketplace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owner agency. */
    @Column(name = "agency_id", nullable = false)
    private Long agencyId;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MarketplaceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "catalog_type", nullable = false, length = 16)
    private MarketplaceCatalogType catalogType;

    /**
     * If catalogType = EXTERNAL_API, the platform fetches products from:
     * GET {apiBaseUrl}/products
     */
    @Column(name = "api_base_url", length = 512)
    private String apiBaseUrl;

    /** Optional header name for external API calls (ex: Authorization). */
    @Column(name = "api_auth_header", length = 64)
    private String apiAuthHeader;

    /** Optional header value (ex: Bearer xxx). */
    @Column(name = "api_auth_value", length = 512)
    private String apiAuthValue;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = MarketplaceStatus.ACTIVE;
        if (catalogType == null) catalogType = MarketplaceCatalogType.MANUAL;
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}

