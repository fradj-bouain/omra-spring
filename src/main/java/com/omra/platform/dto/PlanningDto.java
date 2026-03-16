package com.omra.platform.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningDto {

    private Long id;
    private Long agencyId;
    private String name;
    private String description;
    private Instant createdAt;
    private List<PlanningItemDto> items;
}
