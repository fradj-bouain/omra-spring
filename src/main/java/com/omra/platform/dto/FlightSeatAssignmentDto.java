package com.omra.platform.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightSeatAssignmentDto {

    private Long id;
    private Long flightId;
    private Long flightSeatId;
    private Long pilgrimId;
    private Instant createdAt;
}
