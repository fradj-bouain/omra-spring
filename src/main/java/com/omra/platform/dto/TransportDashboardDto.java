package com.omra.platform.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportDashboardDto {
    private long vehiclesCount;
    private long offersCount;
    private long activeOffersCount;
    private long reservationsPending;
    private long reservationsConfirmed;
    private long reservationsRejected;
    private long reservationsTotal;
    private List<TransportDashboardRecentReservationDto> recentReservations;
}
