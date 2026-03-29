package com.omra.platform.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDto {

    private long totalPilgrims;
    private long activeGroups;
    private long pendingVisas;
    private BigDecimal paymentsReceived;
    private BigDecimal totalRevenue;

    /** Renseigné pour la vue super-admin (plateforme) ; 0 sinon */
    private long totalAgencies;
    private long activeAgencies;
    private long suspendedAgencies;
    private long expiredAgencies;
    /** Utilisateurs rattachés à une agence (hors comptes plateforme sans agencyId) */
    private long totalAgencyUsers;
}
