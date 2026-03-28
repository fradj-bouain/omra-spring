package com.omra.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralCampaignResponseDto {
    private Long id;
    private Long agencyId;
    private String title;
    private Instant startsAt;
    private Instant endsAt;
    @Builder.Default
    private List<Long> slotRewardTierIds = new ArrayList<>();
    private Integer maxWinners;
    private String status;
    private Instant createdAt;
    private Instant closedAt;
}
