package com.omra.platform.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskTemplateDto {

    private Long id;
    private Long agencyId;
    private String name;
    private Integer durationMinutes;
    private Instant createdAt;
}
