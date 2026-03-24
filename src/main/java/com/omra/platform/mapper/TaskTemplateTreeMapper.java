package com.omra.platform.mapper;

import com.omra.platform.dto.TaskTemplateDto;
import com.omra.platform.entity.TaskTemplate;

/** DTO uniquement (évite la récursion JSON sur l'entité). */
public final class TaskTemplateTreeMapper {

    private TaskTemplateTreeMapper() {}

    public static TaskTemplateDto toNode(TaskTemplate e) {
        if (e == null) {
            return null;
        }
        Long parentId = e.getParent() != null ? e.getParent().getId() : null;
        return TaskTemplateDto.builder()
                .id(e.getId())
                .agencyId(e.getAgencyId())
                .name(e.getName())
                .description(e.getDescription())
                .durationMinutes(e.getDurationMinutes())
                .parentId(parentId)
                .createdAt(e.getCreatedAt())
                .build();
    }
}
