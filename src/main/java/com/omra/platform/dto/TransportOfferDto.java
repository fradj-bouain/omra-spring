package com.omra.platform.dto;

import com.omra.platform.entity.enums.TransportOfferStatus;
import com.omra.platform.entity.enums.TransportPricingUnit;
import com.omra.platform.entity.enums.TransportVehicleType;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportOfferDto {
    private Long id;
    private Long vehicleId;
    /** TRANSPORT agency owning the offer (browse API). */
    private Long transportAgencyId;
    private String transportAgencyName;
    private TransportVehicleType vehicleType;
    private Integer vehicleSeatCount;
    private String vehiclePlate;
    private String vehicleBrand;
    private String title;
    private String description;
    private String imageUrl;
    private TransportOfferStatus status;
    private TransportPricingUnit pricingUnit;
    private BigDecimal price;
    private String currency;
    private Integer minUnits;
    private Integer maxUnits;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Instant createdAt;
}
