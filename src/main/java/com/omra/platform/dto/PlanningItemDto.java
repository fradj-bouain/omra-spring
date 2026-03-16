package com.omra.platform.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningItemDto {

    private Long id;
    private Long taskTemplateId;
    private String taskTemplateName;
    private Integer durationMinutes;
    private Integer sortOrder;
}
