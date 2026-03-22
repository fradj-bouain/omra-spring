package com.omra.platform.dto;

import com.omra.platform.entity.enums.ReferralStatus;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralDto {

    private Long id;
    private Long referrerId;
    private Long referredId;
    private ReferralStatus status;
    private Boolean rewardGiven;
    private Instant rewardGrantedAt;
    private Instant createdAt;
}
