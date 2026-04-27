package com.omra.platform.dto;

import com.omra.platform.entity.enums.HotelPricingUnit;
import com.omra.platform.entity.enums.HotelOfferStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelOfferDto {

    private Long id;
    private Long propertyId;
    /** HOTEL agency that owns the offer (for browse API). */
    private Long hotelAgencyId;
    private String hotelAgencyName;
    /** Property snapshot fields for browse UI. */
    private String propertyName;
    private String propertyCity;
    private String propertyCountry;
    private String propertyAddress;
    private String propertyImageUrl;
    private String title;
    private String description;
    private String imageUrl;
    private HotelOfferStatus status;
    private HotelPricingUnit pricingUnit;
    private BigDecimal price;
    private String currency;
    private Integer minUnits;
    private Integer maxUnits;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Instant createdAt;
}
