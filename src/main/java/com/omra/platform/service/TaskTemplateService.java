package com.omra.platform.service;

import com.omra.platform.dto.TaskTemplateDto;
import com.omra.platform.dto.TaskTemplateTotalDurationResponse;
import com.omra.platform.entity.TaskTemplate;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.mapper.TaskTemplateTreeMapper;
import com.omra.platform.repository.TaskTemplateRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskTemplateService {

    private static final int MAX_NESTING_LEVELS = 3;

    private final TaskTemplateRepository taskTemplateRepository;

    private Long requireAgencyIdOrInferFromParent(TaskTemplateDto dto) {
        Long ctx = TenantContext.getAgencyId();
        if (ctx != null) {
            return ctx;
        }
        if (dto.getParentId() != null) {
            TaskTemplate parent = taskTemplateRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("TaskTemplate", dto.getParentId()));
            if (parent.getDeletedAt() != null) {
                throw new ResourceNotFoundException("TaskTemplate", dto.getParentId());
            }
            if (!TenantContext.isSuperAdmin()) {
                throw new ForbiddenException("Agency context required");
            }
            return parent.getAgencyId();
        }
        throw new ForbiddenException("Agency context required");
    }

    private void assertAgencyAccess(Long agencyId) {
        if (TenantContext.isSuperAdmin()) {
            return;
        }
        Long ctx = TenantContext.getAgencyId();
        if (ctx == null || !ctx.equals(agencyId)) {
            throw new ForbiddenException("Access denied");
        }
    }

    private int depthFromRoot(TaskTemplate node) {
        int d = 0;
        TaskTemplate p = node;
        Set<Long> seen = new HashSet<>();
        while (p.getParent() != null) {
            if (!seen.add(p.getId())) {
                throw new BadRequestException("Cycle detected in task template hierarchy");
            }
            d++;
            if (d > MAX_NESTING_LEVELS) {
                throw new BadRequestException("Invalid hierarchy (too deep)");
            }
            p = p.getParent();
        }
        return d;
    }

    @Transactional(readOnly = true)
    public List<TaskTemplateDto> listByAgency() {
        Long agencyId = requireAgencyId();
        if (agencyId == null && TenantContext.isSuperAdmin()) {
            return taskTemplateRepository.findAll().stream()
                    .filter(t -> t.getDeletedAt() == null)
                    .map(this::toDtoFlat)
                    .sorted(Comparator.comparing(TaskTemplateDto::getName, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());
        }
        if (agencyId == null) {
            throw new ForbiddenException("Agency context required");
        }
        return taskTemplateRepository.findByAgencyIdAndDeletedAtIsNullOrderByNameAsc(agencyId).stream()
                .map(this::toDtoFlat)
                .collect(Collectors.toList());
    }

    private Long requireAgencyId() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null && !TenantContext.isSuperAdmin()) {
            throw new ForbiddenException("Agency context required");
        }
        return agencyId;
    }

    @Transactional(readOnly = true)
    public List<TaskTemplateDto> getTree() {
        List<TaskTemplate> all = loadAllForTreeContext();
        return buildForest(all);
    }

    @Transactional(readOnly = true)
    public TaskTemplateDto getByIdAsTree(Long id) {
        TaskTemplate t = findByIdAndAgency(id);
        List<TaskTemplate> all = loadAllForAgency(t.getAgencyId());
        Map<Long, TaskTemplateDto> index = buildForestIndex(all);
        TaskTemplateDto node = index.get(id);
        if (node == null) {
            throw new ResourceNotFoundException("TaskTemplate", id);
        }
        return node;
    }

    @Transactional(readOnly = true)
    public TaskTemplateDto getById(Long id) {
        return toDtoFlat(findByIdAndAgency(id));
    }

    @Transactional(readOnly = true)
    public TaskTemplateTotalDurationResponse getTotalDuration(Long id) {
        TaskTemplate t = findByIdAndAgency(id);
        List<TaskTemplate> all = loadAllForAgency(t.getAgencyId());
        Map<Long, List<TaskTemplate>> byParent = groupChildrenByParentId(all);
        int total = sumDurationRecursive(t.getId(), byParent, taskMapById(all));
        return TaskTemplateTotalDurationResponse.builder()
                .taskTemplateId(id)
                .totalDurationMinutes(total)
                .build();
    }

    @Transactional
    public TaskTemplateDto create(TaskTemplateDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("Le nom de la tâche est requis.");
        }
        Long agencyId = requireAgencyIdOrInferFromParent(dto);

        TaskTemplate parent = null;
        if (dto.getParentId() != null) {
            parent = taskTemplateRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("TaskTemplate", dto.getParentId()));
            if (parent.getDeletedAt() != null) {
                throw new ResourceNotFoundException("TaskTemplate", dto.getParentId());
            }
            if (!parent.getAgencyId().equals(agencyId)) {
                throw new BadRequestException("Parent template belongs to another agency");
            }
            int parentDepth = depthFromRoot(parent);
            if (parentDepth + 1 >= MAX_NESTING_LEVELS) {
                throw new BadRequestException(
                        "Maximum nesting depth is " + MAX_NESTING_LEVELS + " levels");
            }
        }

        TaskTemplate t = TaskTemplate.builder()
                .agencyId(agencyId)
                .name(dto.getName().trim())
                .description(dto.getDescription() != null ? dto.getDescription().trim() : null)
                .durationMinutes(dto.getDurationMinutes() != null ? dto.getDurationMinutes() : 0)
                .parent(parent)
                .build();

        if (parent != null) {
            parent.getChildren().add(t);
        }

        t = taskTemplateRepository.save(t);
        return toDtoFlat(t);
    }

    @Transactional
    public TaskTemplateDto update(Long id, TaskTemplateDto dto) {
        TaskTemplate t = findByIdAndAgency(id);
        if (dto.getName() != null) {
            t.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            t.setDescription(dto.getDescription().trim());
        }
        if (dto.getDurationMinutes() != null) {
            t.setDurationMinutes(dto.getDurationMinutes());
        }
        t = taskTemplateRepository.save(t);
        return toDtoFlat(t);
    }

    @Transactional
    public void delete(Long id) {
        TaskTemplate t = findByIdAndAgency(id);
        softDeleteSubtree(t);
    }

    private void softDeleteSubtree(TaskTemplate t) {
        List<TaskTemplate> kids = taskTemplateRepository
                .findByAgencyIdAndParent_IdAndDeletedAtIsNullOrderByNameAsc(t.getAgencyId(), t.getId());
        for (TaskTemplate k : kids) {
            softDeleteSubtree(k);
        }
        t.setDeletedAt(Instant.now());
        taskTemplateRepository.save(t);
    }

    TaskTemplate findByIdAndAgency(Long id) {
        TaskTemplate t = taskTemplateRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("TaskTemplate", id));
        Long agencyId = TenantContext.getAgencyId();
        if (!TenantContext.isSuperAdmin() && (agencyId == null || !agencyId.equals(t.getAgencyId()))) {
            throw new ForbiddenException("Access denied");
        }
        if (t.getDeletedAt() != null) {
            throw new ResourceNotFoundException("TaskTemplate", id);
        }
        return t;
    }

    private TaskTemplateDto toDtoFlat(TaskTemplate e) {
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

    private List<TaskTemplate> loadAllForTreeContext() {
        Long agencyId = TenantContext.getAgencyId();
        if (TenantContext.isSuperAdmin() && agencyId == null) {
            return taskTemplateRepository.findAllNonDeletedWithParent();
        }
        if (agencyId == null) {
            throw new ForbiddenException("Agency context required");
        }
        return taskTemplateRepository.findAllByAgencyWithParent(agencyId);
    }

    private List<TaskTemplate> loadAllForAgency(Long agencyId) {
        return taskTemplateRepository.findAllByAgencyWithParent(agencyId);
    }

    private List<TaskTemplateDto> buildForest(List<TaskTemplate> all) {
        Map<Long, TaskTemplateDto> index = buildForestIndex(all);
        List<TaskTemplateDto> roots = new ArrayList<>();
        for (TaskTemplate t : all) {
            if (t.getParent() == null) {
                roots.add(index.get(t.getId()));
            }
        }
        roots.sort(Comparator.comparing(TaskTemplateDto::getName, String.CASE_INSENSITIVE_ORDER));
        return roots;
    }

    private Map<Long, TaskTemplateDto> buildForestIndex(List<TaskTemplate> all) {
        Map<Long, TaskTemplateDto> index = new LinkedHashMap<>();
        for (TaskTemplate t : all) {
            index.put(t.getId(), TaskTemplateTreeMapper.toNode(t));
        }
        for (TaskTemplate t : all) {
            if (t.getParent() != null) {
                TaskTemplateDto parentDto = index.get(t.getParent().getId());
                TaskTemplateDto childDto = index.get(t.getId());
                if (parentDto != null && childDto != null) {
                    parentDto.getChildren().add(childDto);
                }
            }
        }
        for (TaskTemplateDto node : index.values()) {
            node.getChildren().sort(Comparator.comparing(TaskTemplateDto::getName, String.CASE_INSENSITIVE_ORDER));
        }
        return index;
    }

    private Map<Long, TaskTemplate> taskMapById(List<TaskTemplate> all) {
        return all.stream().collect(Collectors.toMap(TaskTemplate::getId, x -> x));
    }

    private Map<Long, List<TaskTemplate>> groupChildrenByParentId(List<TaskTemplate> all) {
        Map<Long, List<TaskTemplate>> map = new HashMap<>();
        for (TaskTemplate t : all) {
            if (t.getParent() != null) {
                map.computeIfAbsent(t.getParent().getId(), k -> new ArrayList<>()).add(t);
            }
        }
        for (List<TaskTemplate> kids : map.values()) {
            kids.sort(Comparator.comparing(TaskTemplate::getName, String.CASE_INSENSITIVE_ORDER));
        }
        return map;
    }

    private int sumDurationRecursive(
            Long templateId,
            Map<Long, List<TaskTemplate>> byParent,
            Map<Long, TaskTemplate> byId) {
        TaskTemplate t = byId.get(templateId);
        int m = t != null && t.getDurationMinutes() != null ? t.getDurationMinutes() : 0;
        for (TaskTemplate c : byParent.getOrDefault(templateId, List.of())) {
            m += sumDurationRecursive(c.getId(), byParent, byId);
        }
        return m;
    }
}
