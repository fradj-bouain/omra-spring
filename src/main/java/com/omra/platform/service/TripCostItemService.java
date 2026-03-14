package com.omra.platform.service;

import com.omra.platform.dto.TripCostItemDto;
import com.omra.platform.entity.TripCostItem;
import com.omra.platform.entity.UmrahGroup;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.TripCostItemRepository;
import com.omra.platform.repository.UmrahGroupRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripCostItemService {

    private final TripCostItemRepository tripCostItemRepository;
    private final UmrahGroupRepository umrahGroupRepository;

    private void ensureGroupAccess(Long groupId) {
        UmrahGroup g = umrahGroupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("Group", groupId));
        Long agencyId = TenantContext.getAgencyId();
        if (!TenantContext.isSuperAdmin() && (agencyId == null || !agencyId.equals(g.getAgencyId())))
            throw new ForbiddenException("Access denied");
    }

    @Transactional(readOnly = true)
    public List<TripCostItemDto> getByGroup(Long groupId) {
        ensureGroupAccess(groupId);
        return tripCostItemRepository.findByGroupIdAndDeletedAtIsNull(groupId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TripCostItemDto getById(Long id) {
        TripCostItem item = tripCostItemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("TripCostItem", id));
        ensureGroupAccess(item.getGroupId());
        if (item.getDeletedAt() != null) throw new ResourceNotFoundException("TripCostItem", id);
        return toDto(item);
    }

    @Transactional
    public TripCostItemDto create(TripCostItemDto dto) {
        if (dto.getGroupId() == null) throw new IllegalArgumentException("Group ID required");
        ensureGroupAccess(dto.getGroupId());
        TripCostItem item = TripCostItem.builder()
                .groupId(dto.getGroupId())
                .type(dto.getType())
                .amount(dto.getAmount())
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "MAD")
                .description(dto.getDescription())
                .build();
        item = tripCostItemRepository.save(item);
        return toDto(item);
    }

    @Transactional
    public TripCostItemDto update(Long id, TripCostItemDto dto) {
        TripCostItem item = tripCostItemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("TripCostItem", id));
        ensureGroupAccess(item.getGroupId());
        if (item.getDeletedAt() != null) throw new ResourceNotFoundException("TripCostItem", id);
        if (dto.getType() != null) item.setType(dto.getType());
        if (dto.getAmount() != null) item.setAmount(dto.getAmount());
        if (dto.getCurrency() != null) item.setCurrency(dto.getCurrency());
        if (dto.getDescription() != null) item.setDescription(dto.getDescription());
        item = tripCostItemRepository.save(item);
        return toDto(item);
    }

    @Transactional
    public void delete(Long id) {
        TripCostItem item = tripCostItemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("TripCostItem", id));
        ensureGroupAccess(item.getGroupId());
        item.setDeletedAt(Instant.now());
        tripCostItemRepository.save(item);
    }

    private TripCostItemDto toDto(TripCostItem e) {
        return TripCostItemDto.builder()
                .id(e.getId())
                .groupId(e.getGroupId())
                .type(e.getType())
                .amount(e.getAmount())
                .currency(e.getCurrency())
                .description(e.getDescription())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
