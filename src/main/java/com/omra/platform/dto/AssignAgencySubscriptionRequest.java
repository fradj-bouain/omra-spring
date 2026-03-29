package com.omra.platform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignAgencySubscriptionRequest {

    @NotNull
    private Long planId;

    @NotNull
    private LocalDate periodStart;

    @NotNull
    private LocalDate periodEnd;

    /** Si true : statut ACTIVE + paidAt maintenant (abonnement considéré réglé). */
    @Builder.Default
    private boolean markAsPaid = true;

    private String paymentReference;
    private BigDecimal amountPaid;
    private String currency;
    private String notes;
}
