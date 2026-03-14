package com.omra.platform.controller;

import com.omra.platform.dto.PageResponse;
import com.omra.platform.dto.TaskDto;
import com.omra.platform.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Tasks and subtasks")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    @Operation(summary = "List tasks (optional filter by groupId or assignedToUserId)")
    public ResponseEntity<PageResponse<TaskDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) Long assignedToUserId) {
        return ResponseEntity.ok(taskService.getTasks(PageRequest.of(page, size), groupId, assignedToUserId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<TaskDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create task")
    public ResponseEntity<TaskDto> create(@RequestBody TaskDto dto) {
        return ResponseEntity.ok(taskService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task")
    public ResponseEntity<TaskDto> update(@PathVariable Long id, @RequestBody TaskDto dto) {
        return ResponseEntity.ok(taskService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task (soft)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/subtasks")
    @Operation(summary = "Add subtask")
    public ResponseEntity<TaskDto> addSubtask(@PathVariable Long taskId, @RequestBody TaskDto.SubtaskDto dto) {
        return ResponseEntity.ok(taskService.addSubtask(taskId, dto));
    }

    @PatchMapping("/{taskId}/subtasks/{subtaskId}")
    @Operation(summary = "Update subtask completion")
    public ResponseEntity<TaskDto> updateSubtask(
            @PathVariable Long taskId,
            @PathVariable Long subtaskId,
            @RequestParam boolean completed) {
        return ResponseEntity.ok(taskService.updateSubtask(taskId, subtaskId, completed));
    }
}
