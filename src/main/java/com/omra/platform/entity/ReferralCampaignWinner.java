package com.omra.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "referral_campaign_winners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralCampaignWinner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;

    @Column(name = "pilgrim_id", nullable = false)
    private Long pilgrimId;

    @Column(name = "rank_order", nullable = false)
    private Integer rankOrder;

    @Column(name = "won_at", nullable = false)
    private Instant wonAt;

    @Column(name = "points_at_win", nullable = false)
    private Integer pointsAtWin;

    /** Palier / cadeau attribué pour ce rang (figé au moment du gain). */
    @Column(name = "reward_tier_id")
    private Long rewardTierId;

    @PrePersist
    protected void onCreate() {
        if (wonAt == null) {
            wonAt = Instant.now();
        }
    }
}
