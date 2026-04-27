package com.omra.platform.entity;

import com.omra.platform.entity.enums.HotelPricingUnit;
import com.omra.platform.entity.enums.HotelOfferStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "hotel_offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agency_id", nullable = false)
    private Long agencyId;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    @Column(nullable = false, length = 220)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private HotelOfferStatus status = HotelOfferStatus.DISABLED;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_unit", nullable = false, length = 16)
    private HotelPricingUnit pricingUnit;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(nullable = false, length = 8)
    private String currency;

    @Column(name = "min_units")
    private Integer minUnits;

    @Column(name = "max_units")
    private Integer maxUnits;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;

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
