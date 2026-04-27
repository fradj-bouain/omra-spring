package com.omra.platform.dto;

import com.omra.platform.entity.enums.TransportPricingUnit;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportOfferWriteDto {
    private Long vehicleId;
    private String title;
    private String description;
    private String imageUrl;
    private TransportPricingUnit pricingUnit;
    private BigDecimal price;
    private String currency;
    private Integer minUnits;
    private Integer maxUnits;
    private LocalDate validFrom;
    private LocalDate validTo;
}
