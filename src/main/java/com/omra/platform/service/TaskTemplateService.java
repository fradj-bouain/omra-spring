package com.omra.platform.service;

import com.omra.platform.dto.TaskTemplateDto;
import com.omra.platform.entity.TaskTemplate;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.TaskTemplateRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskTemplateService {

    private final TaskTemplateRepository taskTemplateRepository;

    private Long requireAgencyId() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null && !TenantContext.isSuperAdmin())
            throw new ForbiddenException("Agency context required");
        return agencyId;
    }

    @Transactional(readOnly = true)
    public List<TaskTemplateDto> listByAgency() {
        Long agencyId = requireAgencyId();
        return taskTemplateRepository.findByAgencyIdAndDeletedAtIsNullOrderByName(agencyId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskTemplateDto getById(Long id) {
        TaskTemplate t = findByIdAndAgency(id);
        return toDto(t);
    }

    @Transactional
    public TaskTemplateDto create(TaskTemplateDto dto) {
        Long agencyId = requireAgencyId();
        if (dto.getName() == null || dto.getName().isBlank())
            throw new BadRequestException("Le nom de la tâche est requis.");
        TaskTemplate t = TaskTemplate.builder()
                .agencyId(agencyId)
                .name(dto.getName().trim())
                .durationMinutes(dto.getDurationMinutes() != null ? dto.getDurationMinutes() : 0)
                .build();
        t = taskTemplateRepository.save(t);
        return toDto(t);
    }

    @Transactional
    public TaskTemplateDto update(Long id, TaskTemplateDto dto) {
        TaskTemplate t = findByIdAndAgency(id);
        if (dto.getName() != null) t.setName(dto.getName().trim());
        if (dto.getDurationMinutes() != null) t.setDurationMinutes(dto.getDurationMinutes());
        t = taskTemplateRepository.save(t);
        return toDto(t);
    }

    @Transactional
    public void delete(Long id) {
        TaskTemplate t = findByIdAndAgency(id);
        t.setDeletedAt(java.time.Instant.now());
        taskTemplateRepository.save(t);
    }

    TaskTemplate findByIdAndAgency(Long id) {
        TaskTemplate t = taskTemplateRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("TaskTemplate", id));
        Long agencyId = TenantContext.getAgencyId();
        if (!TenantContext.isSuperAdmin() && (agencyId == null || !agencyId.equals(t.getAgencyId())))
            throw new ForbiddenException("Access denied");
        if (t.getDeletedAt() != null) throw new ResourceNotFoundException("TaskTemplate", id);
        return t;
    }

    private TaskTemplateDto toDto(TaskTemplate e) {
        return TaskTemplateDto.builder()
                .id(e.getId())
                .agencyId(e.getAgencyId())
                .name(e.getName())
                .durationMinutes(e.getDurationMinutes())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
