package com.omra.platform.dto;

import com.omra.platform.entity.enums.TransportVehicleType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportVehicleWriteDto {
    private TransportVehicleType vehicleType;
    private Integer seatCount;
    private String plate;
    private String brand;
    private String driverName;
    private String driverPhone;
    private String driverEmail;
    private String address;
}
