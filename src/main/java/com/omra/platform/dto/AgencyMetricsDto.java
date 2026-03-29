package com.omra.platform.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencyMetricsDto {

    private long userCount;
    private long pilgrimCount;
    private long groupCount;
    /** Somme des paiements encaissés (statut PAID) pour cette agence */
    private BigDecimal revenuePaid;
}
