package com.omra.platform.dto;

import com.omra.platform.entity.enums.HotelCity;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupHotelDto {

    private Long id;
    private Long groupId;
    private Long hotelId;
    private HotelCity city;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private String roomType;
    private HotelDto hotel;
}
