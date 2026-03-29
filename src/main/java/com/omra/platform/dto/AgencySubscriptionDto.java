package com.omra.platform.dto;

import com.omra.platform.entity.enums.AgencySubscriptionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencySubscriptionDto {

    private Long id;
    private Long agencyId;
    private Long planId;
    private String planCode;
    private String planName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private AgencySubscriptionStatus status;
    private Instant paidAt;
    private String paymentReference;
    private BigDecimal amountPaid;
    private String currency;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
}
