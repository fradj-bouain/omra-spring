package com.omra.platform.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralStatsDto {

    private String referralCode;
    private String referralLink;
    private long totalReferrals;
    private long pendingReferrals;
    private long completedReferrals;
    private long rewardsGranted;
}
