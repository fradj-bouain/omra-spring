package com.omra.platform.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardGroupKpiDto {

    private Long groupId;
    private String groupName;
    private int filledCapacity;
    private int maxCapacity;
    private BigDecimal totalPaid;
    private BigDecimal price;
    private String status;
}
