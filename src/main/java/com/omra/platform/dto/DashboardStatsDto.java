package com.omra.platform.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDto {

    private long totalPilgrims;
    private long activeGroups;
    private long pendingVisas;
    private BigDecimal paymentsReceived;
    private BigDecimal totalRevenue;
}
