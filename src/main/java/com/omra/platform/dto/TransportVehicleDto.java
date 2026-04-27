package com.omra.platform.dto;

import com.omra.platform.entity.enums.TransportVehicleType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportVehicleDto {
    private Long id;
    private TransportVehicleType vehicleType;
    private Integer seatCount;
    private String plate;
    private String brand;
    private String driverName;
    private String driverPhone;
    private String driverEmail;
    private String address;
    private Instant createdAt;
}
