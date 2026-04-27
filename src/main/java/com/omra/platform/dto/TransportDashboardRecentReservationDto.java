package com.omra.platform.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportDashboardRecentReservationDto {
    private Long id;
    private String contactName;
    private String status;
    private Instant createdAt;
    private Integer units;
    private String travelAgencyName;
}
