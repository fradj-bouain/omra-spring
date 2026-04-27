package com.omra.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelDashboardDto {
    private long propertiesCount;
    private long offersCount;
    private long activeOffersCount;
    private long reservationsPending;
    private long reservationsConfirmed;
    private long reservationsRejected;
    private long reservationsTotal;
    /** Dernières demandes (pour le tableau de bord). */
    private List<HotelDashboardRecentReservationDto> recentReservations;
}
