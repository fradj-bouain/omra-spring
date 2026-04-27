package com.omra.platform.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelReservationCreateDto {
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private Integer units;
    private LocalDate desiredFrom;
    private LocalDate desiredTo;
    private String note;
}

