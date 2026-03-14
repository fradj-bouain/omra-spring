package com.omra.platform.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusDto {

    private Long id;
    private Long agencyId;
    private String plate;
    private Integer capacity;
    private Instant createdAt;
}
