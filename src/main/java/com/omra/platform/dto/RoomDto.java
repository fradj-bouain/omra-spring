package com.omra.platform.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDto {

    private Long id;
    private Long hotelId;
    private String roomNumber;
    private Integer capacity;
    private String roomType;
    private Instant createdAt;
}
