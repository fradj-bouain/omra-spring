package com.omra.platform.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralRewardTierDto {

    private Long id;
    private Long agencyId;
    private Integer pointsThreshold;
    private String giftTitle;
    private String giftDescription;
    private Integer sortOrder;
}
