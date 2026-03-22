package com.omra.platform.service;

import com.omra.platform.dto.PlanningDto;
import com.omra.platform.dto.PlanningItemDto;
import com.omra.platform.entity.Planning;
import com.omra.platform.entity.PlanningItem;
import com.omra.platform.entity.TaskTemplate;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.PlanningItemRepository;
import com.omra.platform.repository.PlanningRepository;
import com.omra.platform.repository.TaskTemplateRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanningService {

    private final PlanningRepository planningRepository;
    private final PlanningItemRepository planningItemRepository;
    private final TaskTemplateService taskTemplateService;
    private final TaskTemplateRepository taskTemplateRepository;

    private Long requireAgencyId() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null && !TenantContext.isSuperAdmin())
            throw new ForbiddenException("Agency context required");
        return agencyId;
    }

    @Transactional(readOnly = true)
    public List<PlanningDto> listByAgency() {
        Long agencyId = requireAgencyId();
        List<Planning> plannings = planningRepository.findByAgencyIdAndDeletedAtIsNullOrderByName(agencyId);
        return plannings.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlanningDto getById(Long id) {
        Planning p = findByIdAndAgency(id);
        return toDto(p);
    }

    /**
     * Lecture d'un planning pour une agence donnée (ex. app mobile accompagnateur, hors TenantContext).
     */
    @Transactional(readOnly = true)
    public PlanningDto getByIdForAgency(Long id, Long agencyId) {
        Planning p = planningRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Planning", id));
        if (p.getDeletedAt() != null) throw new ResourceNotFoundException("Planning", id);
        if (!p.getAgencyId().equals(agencyId)) throw new ForbiddenException("Access denied");
        return toDto(p);
    }

    @Transactional
    public PlanningDto create(PlanningDto dto) {
        Long agencyId = requireAgencyId();
        if (dto.getName() == null || dto.getName().isBlank())
            throw new BadRequestException("Le nom du planning est requis.");
        Planning p = Planning.builder()
                .agencyId(agencyId)
                .name(dto.getName().trim())
                .description(dto.getDescription() != null ? dto.getDescription().trim() : null)
                .build();
        p = planningRepository.save(p);
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            List<PlanningItem> items = new ArrayList<>();
            for (int i = 0; i < dto.getItems().size(); i++) {
                PlanningItemDto it = dto.getItems().get(i);
                Long ttId = it.getTaskTemplateId();
                if (ttId == null) continue;
                taskTemplateService.findByIdAndAgency(ttId); // check access
                items.add(PlanningItem.builder()
                        .planningId(p.getId())
                        .taskTemplateId(ttId)
                        .sortOrder(it.getSortOrder() != null ? it.getSortOrder() : i)
                        .build());
            }
            planningItemRepository.saveAll(items);
        }
        return getById(p.getId());
    }

    @Transactional
    public PlanningDto update(Long id, PlanningDto dto) {
        Planning p = findByIdAndAgency(id);
        if (dto.getName() != null) p.setName(dto.getName().trim());
        if (dto.getDescription() != null) p.setDescription(dto.getDescription().trim());
        p = planningRepository.save(p);
        if (dto.getItems() != null) {
            planningItemRepository.deleteByPlanningId(p.getId());
            List<PlanningItem> items = new ArrayList<>();
            for (int i = 0; i < dto.getItems().size(); i++) {
                PlanningItemDto it = dto.getItems().get(i);
                Long ttId = it.getTaskTemplateId();
                if (ttId == null) continue;
                taskTemplateService.findByIdAndAgency(ttId);
                items.add(PlanningItem.builder()
                        .planningId(p.getId())
                        .taskTemplateId(ttId)
                        .sortOrder(it.getSortOrder() != null ? it.getSortOrder() : i)
                        .build());
            }
            planningItemRepository.saveAll(items);
        }
        return getById(p.getId());
    }

    @Transactional
    public void delete(Long id) {
        Planning p = findByIdAndAgency(id);
        p.setDeletedAt(java.time.Instant.now());
        planningRepository.save(p);
    }

    Planning findByIdAndAgency(Long id) {
        Planning p = planningRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Planning", id));
        Long agencyId = TenantContext.getAgencyId();
        if (!TenantContext.isSuperAdmin() && (agencyId == null || !agencyId.equals(p.getAgencyId())))
            throw new ForbiddenException("Access denied");
        if (p.getDeletedAt() != null) throw new ResourceNotFoundException("Planning", id);
        return p;
    }

    private PlanningDto toDto(Planning p) {
        List<PlanningItem> items = planningItemRepository.findByPlanningIdOrderBySortOrderAsc(p.getId());
        List<Long> ttIds = items.stream().map(PlanningItem::getTaskTemplateId).distinct().toList();
        Map<Long, TaskTemplate> templates = taskTemplateRepository.findAllById(ttIds).stream().collect(Collectors.toMap(TaskTemplate::getId, t -> t));
        List<PlanningItemDto> itemDtos = items.stream()
                .map(it -> {
                    TaskTemplate tt = templates.get(it.getTaskTemplateId());
                    return PlanningItemDto.builder()
                            .id(it.getId())
                            .taskTemplateId(it.getTaskTemplateId())
                            .taskTemplateName(tt != null ? tt.getName() : null)
                            .durationMinutes(tt != null ? tt.getDurationMinutes() : null)
                            .sortOrder(it.getSortOrder())
                            .build();
                })
                .collect(Collectors.toList());
        return PlanningDto.builder()
                .id(p.getId())
                .agencyId(p.getAgencyId())
                .name(p.getName())
                .description(p.getDescription())
                .createdAt(p.getCreatedAt())
                .items(itemDtos)
                .build();
    }
}
