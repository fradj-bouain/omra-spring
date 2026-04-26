package com.omra.platform.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelPropertyWriteDto {

    private String name;
    private String description;
    private String city;
    private String country;
    private String address;
    private String imageUrl;
}
