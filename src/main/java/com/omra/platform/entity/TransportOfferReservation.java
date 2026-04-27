package com.omra.platform.entity;

import com.omra.platform.entity.enums.HotelReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "transport_offer_reservations", indexes = {
        @Index(name = "idx_transport_offer_resv_offer", columnList = "offer_id"),
        @Index(name = "idx_transport_offer_resv_transport_agency", columnList = "transport_agency_id, created_at"),
        @Index(name = "idx_transport_offer_resv_travel_agency", columnList = "travel_agency_id, created_at"),
        @Index(name = "idx_transport_offer_resv_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportOfferReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "offer_id", nullable = false)
    private Long offerId;

    @Column(name = "transport_agency_id", nullable = false)
    private Long transportAgencyId;

    @Column(name = "travel_agency_id", nullable = false)
    private Long travelAgencyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private HotelReservationStatus status = HotelReservationStatus.PENDING;

    @Column(name = "contact_name", nullable = false, length = 140)
    private String contactName;

    @Column(name = "contact_phone", length = 60)
    private String contactPhone;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "units")
    private Integer units;

    @Column(name = "desired_from")
    private LocalDate desiredFrom;

    @Column(name = "desired_to")
    private LocalDate desiredTo;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
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
