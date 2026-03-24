package com.omra.platform.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskTemplateDto {

    private Long id;
    private Long agencyId;
    private String name;
    private String description;
    private Integer durationMinutes;
    private Long parentId;
    private Instant createdAt;

    /** Renseigné pour les réponses arbre (GET /tree, etc.). */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Builder.Default
    private List<TaskTemplateDto> children = new ArrayList<>();
}
