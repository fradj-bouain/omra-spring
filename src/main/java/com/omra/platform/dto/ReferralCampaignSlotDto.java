package com.omra.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralCampaignSlotDto {
    private Integer rankOrder;
    private Long rewardTierId;
    private Integer pointsThreshold;
    private String giftTitle;
    private String giftDescription;
}
