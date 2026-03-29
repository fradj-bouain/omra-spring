package com.omra.platform.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencySubscriptionSummaryDto {

    /** Dernier enregistrement créé (historique). */
    private AgencySubscriptionDto latest;

    /** Couverture payée valide aujourd'hui, si elle existe. */
    private AgencySubscriptionDto currentValid;
}
