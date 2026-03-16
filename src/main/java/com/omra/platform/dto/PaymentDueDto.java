package com.omra.platform.dto;

import com.omra.platform.entity.enums.PaymentDueStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDueDto {

    private Long id;
    private Long paymentId;
    private LocalDate dueDate;
    private BigDecimal amount;
    private PaymentDueStatus status;
    private Integer sequenceOrder;
}
