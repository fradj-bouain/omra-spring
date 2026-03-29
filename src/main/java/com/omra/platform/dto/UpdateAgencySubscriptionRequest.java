package com.omra.platform.dto;

import com.omra.platform.entity.enums.AgencySubscriptionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAgencySubscriptionRequest {

    private LocalDate periodStart;
    private LocalDate periodEnd;
    private AgencySubscriptionStatus status;
    private Boolean markAsPaid;
    private String paymentReference;
    private BigDecimal amountPaid;
    private String currency;
    private String notes;
}
