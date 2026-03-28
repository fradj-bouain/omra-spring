package com.omra.platform.dto;

import com.omra.platform.entity.enums.SponsorType;
import com.omra.platform.entity.enums.VisaStatus;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PilgrimDto {

    private Long id;
    private Long agencyId;
    private String firstName;
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
    private VisaStatus visaStatus;
    private Instant createdAt;

    /** Parrainage : type de parrain (pèlerin ou agent), libellé libre, id du pèlerin parrain. */
    private SponsorType sponsorType;
    private String sponsorLabel;
    private Long referrerPilgrimId;
    /** Nom affiché du parrain (lecture seule, GET détail). */
    private String referrerDisplayName;
    /** Points cumulés comme parrain. */
    private Integer referralPoints;
    /** Prochain palier (points) et titre du cadeau — GET détail. */
    private Integer nextRewardThreshold;
    private String nextRewardTitle;
}
