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
    private String country;
    private Integer stars;
    private String contactImportant;
    private String contactPhone;
    private String receptionPhone;
    private String email;
}
