package com.omra.platform.dto;

import com.omra.platform.entity.enums.MarketplaceOrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopOrderSummaryDto {

    private Long id;
    private Long pilgrimId;
    private MarketplaceOrderStatus status;
    private BigDecimal total;
    private String currency;
    private Instant createdAt;
}
