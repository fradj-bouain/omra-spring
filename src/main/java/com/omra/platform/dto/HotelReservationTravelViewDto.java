package com.omra.platform.dto;

import com.omra.platform.entity.enums.HotelReservationStatus;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Réservation d’offre hôtel telle que vue par l’agence voyage (demande + offre + statut).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelReservationTravelViewDto {
    private Long id;
    private Long offerId;
    private String offerTitle;
    private String propertyName;
    private String hotelAgencyName;
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
