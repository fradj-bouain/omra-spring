package com.omra.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Vue d’ensemble du jeu de parrainage pour l’interface agence.
 * {@code phase}: IDLE | UPCOMING | LIVE | ENDED_TIME | ENDED_FULL
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralCampaignDashboardDto {

    /** Aucune ligne ACTIVE en base pour cette agence. */
    @Builder.Default
    private boolean idle = true;

    private Long campaignId;
    private String title;
    private String status;
    private Instant startsAt;
    private Instant endsAt;
    private Integer maxWinners;
    private int winnersCount;

    /** UPCOMING | LIVE | ENDED_TIME | ENDED_FULL | IDLE */
    private String phase;

    /** Paliers par rang (1, 2, 3…) pour ce jeu. */
    @Builder.Default
    private List<ReferralCampaignSlotDto> slots = new ArrayList<>();

    @Builder.Default
    private List<ReferralCampaignWinnerRowDto> winners = new ArrayList<>();
}
