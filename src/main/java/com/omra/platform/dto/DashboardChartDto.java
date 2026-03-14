package com.omra.platform.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardChartDto {

    /** Payments over time: e.g. [{ "period": "2025-01", "amount": 50000 }, ...] */
    private List<PeriodAmountDto> paymentsOverTime;

    /** Visa status distribution: e.g. [{ "status": "PENDING", "count": 10 }, ...] */
    private List<StatusCountDto> visaDistribution;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodAmountDto {
        private String period;
        private java.math.BigDecimal amount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusCountDto {
        private String status;
        private long count;
    }
}
