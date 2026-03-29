package com.omra.platform.dto;

import com.omra.platform.entity.enums.SubscriptionBillingPeriod;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlanDto {

    private Long id;
    private String code;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private SubscriptionBillingPeriod billingPeriod;
    private Integer defaultDurationDays;
    private Integer maxUsers;
    private String features;
    private Boolean active;
    private Integer sortOrder;
    private Instant createdAt;
    private Instant updatedAt;
}
