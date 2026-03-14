package com.omra.platform.service;

import com.omra.platform.dto.PageResponse;
import com.omra.platform.dto.TaskDto;
import com.omra.platform.entity.Subtask;
import com.omra.platform.entity.Task;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.SubtaskRepository;
import com.omra.platform.repository.TaskRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final SubtaskRepository subtaskRepository;

    private Long requireAgencyId() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null && !TenantContext.isSuperAdmin()) throw new ForbiddenException("Agency context required");
        return agencyId;
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskDto> getTasks(Pageable pageable, Long groupId, Long assignedToUserId) {
        Long agencyId = requireAgencyId();
        Page<Task> page;
        if (groupId != null)
            page = taskRepository.findByAgencyIdAndGroupIdAndDeletedAtIsNull(agencyId, groupId, pageable);
        else if (assignedToUserId != null)
            page = taskRepository.findByAgencyIdAndAssignedToUserIdAndDeletedAtIsNull(agencyId, assignedToUserId, pageable);
        else
            page = taskRepository.findByAgencyIdAndDeletedAtIsNull(agencyId, pageable);
        List<TaskDto> content = page.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return PageResponse.<TaskDto>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public TaskDto getById(Long id) {
        Task task = findByIdAndAgency(id);
        return toDto(task);
    }

    @Transactional
    public TaskDto create(TaskDto dto) {
        Long agencyId = requireAgencyId();
        if (agencyId == null) {
            throw new ForbiddenException("Un contexte agence est requis pour créer une tâche. Connectez-vous en tant qu'utilisateur d'une agence.");
        }
        Task task = Task.builder()
                .agencyId(agencyId)
                .groupId(dto.getGroupId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .dueDate(dto.getDueDate())
                .status(dto.getStatus() != null ? dto.getStatus() : com.omra.platform.entity.enums.TaskStatus.TODO)
                .assignedToUserId(dto.getAssignedToUserId())
                .build();
        task = taskRepository.save(task);
        return toDto(task);
    }

    @Transactional
    public TaskDto update(Long id, TaskDto dto) {
        Task task = findByIdAndAgency(id);
        if (dto.getTitle() != null) task.setTitle(dto.getTitle());
        if (dto.getDescription() != null) task.setDescription(dto.getDescription());
        if (dto.getDueDate() != null) task.setDueDate(dto.getDueDate());
        if (dto.getStatus() != null) task.setStatus(dto.getStatus());
        if (dto.getAssignedToUserId() != null) task.setAssignedToUserId(dto.getAssignedToUserId());
        if (dto.getGroupId() != null) task.setGroupId(dto.getGroupId());
        task = taskRepository.save(task);
        return toDto(task);
    }

    @Transactional
    public void delete(Long id) {
        Task task = findByIdAndAgency(id);
        task.setDeletedAt(Instant.now());
        taskRepository.save(task);
    }

    @Transactional
    public TaskDto addSubtask(Long taskId, TaskDto.SubtaskDto dto) {
        Task task = findByIdAndAgency(taskId);
        Subtask st = Subtask.builder()
                .taskId(taskId)
                .title(dto.getTitle())
                .completed(dto.getCompleted() != null ? dto.getCompleted() : false)
                .build();
        subtaskRepository.save(st);
        return toDto(task);
    }

    @Transactional
    public TaskDto updateSubtask(Long taskId, Long subtaskId, boolean completed) {
        Task task = findByIdAndAgency(taskId);
        Subtask st = subtaskRepository.findById(subtaskId).orElseThrow(() -> new ResourceNotFoundException("Subtask", subtaskId));
        if (!st.getTaskId().equals(taskId)) throw new ResourceNotFoundException("Subtask", subtaskId);
        st.setCompleted(completed);
        subtaskRepository.save(st);
        return toDto(task);
    }

    private Task findByIdAndAgency(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task", id));
        Long agencyId = TenantContext.getAgencyId();
        if (!TenantContext.isSuperAdmin() && (agencyId == null || !agencyId.equals(task.getAgencyId())))
            throw new ForbiddenException("Access denied");
        if (task.getDeletedAt() != null) throw new ResourceNotFoundException("Task", id);
        return task;
    }

    private TaskDto toDto(Task e) {
        List<TaskDto.SubtaskDto> subtasks = subtaskRepository.findByTaskId(e.getId()).stream()
                .map(s -> new TaskDto.SubtaskDto(s.getId(), s.getTaskId(), s.getTitle(), s.getCompleted(), s.getCreatedAt()))
                .collect(Collectors.toList());
        return TaskDto.builder()
                .id(e.getId())
                .agencyId(e.getAgencyId())
                .groupId(e.getGroupId())
                .title(e.getTitle())
                .description(e.getDescription())
                .dueDate(e.getDueDate())
                .status(e.getStatus())
                .assignedToUserId(e.getAssignedToUserId())
                .createdAt(e.getCreatedAt())
                .subtasks(subtasks)
                .build();
    }
}
