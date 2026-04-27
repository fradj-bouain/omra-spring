package com.omra.platform.dto;

import com.omra.platform.entity.enums.HotelReservationStatus;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelReservationDto {
    private Long id;
    private Long offerId;
    private Long hotelAgencyId;
    private Long travelAgencyId;
    private HotelReservationStatus status;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private Integer units;
    private LocalDate desiredFrom;
    private LocalDate desiredTo;
    private String note;
    private Instant createdAt;
}

