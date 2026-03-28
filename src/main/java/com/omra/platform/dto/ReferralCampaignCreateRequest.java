package com.omra.platform.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class ReferralCampaignCreateRequest {
    private String title;
    @NotNull
    private Instant startsAt;
    @NotNull
    private Instant endsAt;

    /**
     * Ordre = rang : index 0 → 1er gagnant, index 1 → 2e, etc.
     * Taille = nombre de places (1–500). Chaque entrée est un {@code referral_reward_tiers.id}.
     */
    @NotEmpty
    @Size(min = 1, max = 500)
    private List<@NotNull Long> slotRewardTierIds;
}
