package com.omra.platform.dto;

import com.omra.platform.entity.enums.TripCostType;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripCostItemDto {

    private Long id;
    private Long groupId;
    private TripCostType type;
    private BigDecimal amount;
    private String currency;
    private String description;
    private Instant createdAt;
}
