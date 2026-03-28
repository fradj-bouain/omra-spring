package com.omra.platform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "referral_campaign_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralCampaignSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;

    @Column(name = "rank_order", nullable = false)
    private Integer rankOrder;

    @Column(name = "reward_tier_id", nullable = false)
    private Long rewardTierId;
}
