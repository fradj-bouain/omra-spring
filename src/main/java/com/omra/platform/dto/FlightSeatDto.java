package com.omra.platform.dto;

import com.omra.platform.entity.enums.SeatStatus;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightSeatDto {

    private Long id;
    private Long flightId;
    private String seatNumber;
    private SeatStatus status;
    private Long pilgrimId; // if assigned
    private Instant createdAt;
}
