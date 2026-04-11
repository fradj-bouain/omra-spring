package com.omra.platform.entity;

import com.omra.platform.entity.enums.SponsorType;
import com.omra.platform.entity.enums.TravelerType;
import com.omra.platform.entity.enums.VisaStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(name = "pilgrims", indexes = {
    @Index(name = "idx_pilgrim_agency_id", columnList = "agency_id"),
    @Index(name = "idx_pilgrim_passport", columnList = "passport_number"),
    @Index(name = "idx_pilgrim_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pilgrim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agency_id", nullable = false)
    private Long agencyId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String gender;
    private LocalDate dateOfBirth;

    private String passportNumber;
    private LocalDate passportIssueDate;
    private LocalDate passportExpiry;

    private String nationality;
    private String phone;
    private String email;
    private String address;

    private String photoUrl;
    private String passportScanUrl;

    @Enumerated(EnumType.STRING)
    private VisaStatus visaStatus;

    /** Motif / catégorie de voyage (pèlerinage, loisirs, travail…). */
    @Enumerated(EnumType.STRING)
    @Column(name = "traveler_type", nullable = false, length = 32)
    @Builder.Default
    private TravelerType travelerType = TravelerType.PILGRIM;

    /** Parrainage : type (autre voyageur vs agent), libellé libre, référence voyageur parrain. */
    @Enumerated(EnumType.STRING)
    @Column(name = "sponsor_type")
    private SponsorType sponsorType;

    @Column(name = "sponsor_label")
    private String sponsorLabel;

    @Column(name = "referrer_pilgrim_id")
    private Long referrerPilgrimId;

    /** Regroupement famille (création batch) — {@code pilgrim_families.id}. */
    @Column(name = "family_id")
    private Long familyId;

    /** PERE, MERE, ENFANT, AUTRE (optionnel). */
    @Column(name = "family_role", length = 20)
    private String familyRole;

    /** Points cumulés en tant que parrain (invitations enregistrées). */
    @Column(name = "referral_points", nullable = false)
    @Builder.Default
    private Integer referralPoints = 0;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
