package com.omra.platform.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelPropertyDto {

    private Long id;
    private String name;
    private String description;
    private String city;
    private String country;
    private String address;
    private String imageUrl;
    private Instant createdAt;
}
