package com.omra.platform.dto;

import com.omra.platform.entity.enums.HotelPricingUnit;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelOfferWriteDto {

    private Long propertyId;
    private String title;
    private String description;
    private String imageUrl;
    private HotelPricingUnit pricingUnit;
    private BigDecimal price;
    private String currency;
    private Integer minUnits;
    private Integer maxUnits;
    private LocalDate validFrom;
    private LocalDate validTo;
}
