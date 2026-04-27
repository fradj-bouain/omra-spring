package com.omra.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelDashboardRecentReservationDto {
    private Long id;
    private String contactName;
    private String status;
    private Instant createdAt;
    private Integer units;
    /** Agence de voyage demandeuse. */
    private String travelAgencyName;
}
