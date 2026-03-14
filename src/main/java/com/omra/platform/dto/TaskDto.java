package com.omra.platform.dto;

import com.omra.platform.entity.enums.TaskStatus;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDto {

    private Long id;
    private Long agencyId;
    private Long groupId;
    private String title;
    private String description;
    private LocalDate dueDate;
    private TaskStatus status;
    private Long assignedToUserId;
    private Instant createdAt;
    private List<SubtaskDto> subtasks;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubtaskDto {
        private Long id;
        private Long taskId;
        private String title;
        private Boolean completed;
        private Instant createdAt;
    }
}
