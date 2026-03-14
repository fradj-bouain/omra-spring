package com.omra.platform.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelDto {

    private Long id;
    private String name;
    private String city;
    private String address;
    private Integer stars;
    private String contactPhone;
}
