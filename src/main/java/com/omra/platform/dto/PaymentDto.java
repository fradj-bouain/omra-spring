package com.omra.platform.dto;

import com.omra.platform.entity.enums.PaymentMethod;
import com.omra.platform.entity.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDto {

    private Long id;
    private Long agencyId;
    private Long pilgrimId;
    private Long groupId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private LocalDate paymentDate;
    private String reference;
    private LocalDate firstDueDate;
    private Integer duePeriodDays;
    private Integer numberOfInstallments;
    private List<PaymentDueDto> dueDates;
    private Instant createdAt;
}
