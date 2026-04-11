package com.omra.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Quota of active sub-agencies vs current subscription plan for a main agency. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubAgencyQuotaDto {

    private int activeSubAgencies;
    /** From plan; {@code null} = unlimited. If no valid subscription, {@code 0}. */
    private Integer maxSubAgencies;
    private boolean canCreate;
}
