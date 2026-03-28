package com.omra.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "pilgrim_sponsor_events", indexes = {
        @Index(name = "idx_sponsor_event_referrer", columnList = "referrer_pilgrim_id"),
        @Index(name = "idx_sponsor_event_agency", columnList = "agency_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PilgrimSponsorEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agency_id", nullable = false)
    private Long agencyId;

    @Column(name = "referrer_pilgrim_id", nullable = false)
    private Long referrerPilgrimId;

    @Column(name = "referred_pilgrim_id", nullable = false, unique = true)
    private Long referredPilgrimId;

    @Column(name = "points_awarded", nullable = false)
    private Integer pointsAwarded;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
