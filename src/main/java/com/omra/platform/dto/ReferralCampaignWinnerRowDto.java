package com.omra.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralCampaignWinnerRowDto {
    private Long pilgrimId;
    private String pilgrimDisplayName;
    private Integer rankOrder;
    private Instant wonAt;
    private Integer pointsAtWin;
    private Long rewardTierId;
    private String giftTitle;
    private String giftDescription;
}
