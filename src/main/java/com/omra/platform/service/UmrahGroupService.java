package com.omra.platform.service;

import com.omra.platform.dto.PageResponse;
import com.omra.platform.dto.PilgrimDto;
import com.omra.platform.dto.UmrahGroupDto;
import com.omra.platform.entity.GroupPilgrim;
import com.omra.platform.entity.Pilgrim;
import com.omra.platform.entity.UmrahGroup;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.AgencyRepository;
import com.omra.platform.repository.GroupPilgrimRepository;
import com.omra.platform.repository.PilgrimRepository;
import com.omra.platform.repository.UmrahGroupRepository;
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
public class UmrahGroupService {

    private final UmrahGroupRepository groupRepository;
    private final GroupPilgrimRepository groupPilgrimRepository;
    private final PilgrimRepository pilgrimRepository;
    private final AgencyRepository agencyRepository;

    private Long requireAgencyId() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null && !TenantContext.isSuperAdmin()) {
            throw new ForbiddenException("Agency context required");
        }
        return agencyId;
    }

    @Transactional(readOnly = true)
    public PageResponse<UmrahGroupDto> getGroups(Pageable pageable) {
        Long agencyId = requireAgencyId();
        if (TenantContext.isSuperAdmin() && agencyId == null) {
            Page<UmrahGroup> page = groupRepository.findByDeletedAtIsNull(pageable);
            return toPageResponse(page);
        }
        Page<UmrahGroup> page = groupRepository.findByAgencyIdAndDeletedAtIsNull(agencyId, pageable);
        return toPageResponse(page);
    }

    @Transactional(readOnly = true)
    public UmrahGroupDto getById(Long id) {
        UmrahGroup group = findByIdAndAgency(id);
        return toDto(group);
    }

    @Transactional
    public UmrahGroupDto create(UmrahGroupDto dto) {
        Long agencyId;
        if (TenantContext.isSuperAdmin()) {
            if (dto.getAgencyId() == null) {
                throw new BadRequestException("agencyId est requis dans le body pour le super admin.");
            }
            if (!agencyRepository.existsById(dto.getAgencyId())) {
                throw new BadRequestException("Agence inexistante.");
            }
            agencyId = dto.getAgencyId();
        } else {
            agencyId = TenantContext.getAgencyId();
            if (agencyId == null) {
                throw new ForbiddenException("Agency context required");
            }
        }
        UmrahGroup group = UmrahGroup.builder()
                .agencyId(agencyId)
                .name(dto.getName())
                .description(dto.getDescription())
                .departureDate(dto.getDepartureDate())
                .returnDate(dto.getReturnDate())
                .maxCapacity(dto.getMaxCapacity())
                .price(dto.getPrice())
                .status(dto.getStatus() != null ? dto.getStatus() : com.omra.platform.entity.enums.GroupStatus.OPEN)
                .build();
        group = groupRepository.save(group);
        return toDto(group);
    }

    @Transactional
    public UmrahGroupDto update(Long id, UmrahGroupDto dto) {
        UmrahGroup group = findByIdAndAgency(id);
        if (dto.getName() != null) group.setName(dto.getName());
        if (dto.getDescription() != null) group.setDescription(dto.getDescription());
        if (dto.getDepartureDate() != null) group.setDepartureDate(dto.getDepartureDate());
        if (dto.getReturnDate() != null) group.setReturnDate(dto.getReturnDate());
        if (dto.getMaxCapacity() != null) group.setMaxCapacity(dto.getMaxCapacity());
        if (dto.getPrice() != null) group.setPrice(dto.getPrice());
        if (dto.getStatus() != null) group.setStatus(dto.getStatus());
        group = groupRepository.save(group);
        return toDto(group);
    }

    @Transactional
    public void addPilgrimToGroup(Long groupId, Long pilgrimId) {
        UmrahGroup group = findByIdAndAgency(groupId);
        Pilgrim pilgrim = pilgrimRepository.findById(pilgrimId)
                .orElseThrow(() -> new ResourceNotFoundException("Pilgrim", pilgrimId));
        if (pilgrim.getDeletedAt() != null) throw new ResourceNotFoundException("Pilgrim", pilgrimId);
        if (!group.getAgencyId().equals(pilgrim.getAgencyId())) {
            throw new BadRequestException("Pilgrim does not belong to the same agency");
        }
        if (groupPilgrimRepository.existsByGroupIdAndPilgrimId(groupId, pilgrimId)) {
            throw new BadRequestException("Pilgrim already in group");
        }
        if (group.getMaxCapacity() != null) {
            long count = groupPilgrimRepository.findByGroupId(groupId).size();
            if (count >= group.getMaxCapacity()) {
                throw new BadRequestException("Group is full");
            }
        }
        GroupPilgrim gp = GroupPilgrim.builder().groupId(groupId).pilgrimId(pilgrimId).build();
        groupPilgrimRepository.save(gp);
    }

    @Transactional
    public void removePilgrimFromGroup(Long groupId, Long pilgrimId) {
        findByIdAndAgency(groupId);
        groupPilgrimRepository.deleteByGroupIdAndPilgrimId(groupId, pilgrimId);
    }

    @Transactional(readOnly = true)
    public List<PilgrimDto> getGroupPilgrims(Long groupId) {
        UmrahGroup group = findByIdAndAgency(groupId);
        return groupPilgrimRepository.findByGroupId(groupId).stream()
                .map(gp -> pilgrimRepository.findById(gp.getPilgrimId()).orElse(null))
                .filter(p -> p != null && p.getDeletedAt() == null)
                .map(this::pilgrimToDto)
                .collect(Collectors.toList());
    }

    private UmrahGroup findByIdAndAgency(Long id) {
        UmrahGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group", id));
        Long agencyId = TenantContext.getAgencyId();
        if (!TenantContext.isSuperAdmin() && (agencyId == null || !agencyId.equals(group.getAgencyId()))) {
            throw new ForbiddenException("Access denied to this group");
        }
        if (group.getDeletedAt() != null) throw new ResourceNotFoundException("Group", id);
        return group;
    }

    private PageResponse<UmrahGroupDto> toPageResponse(Page<UmrahGroup> page) {
        List<UmrahGroupDto> content = page.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return PageResponse.<UmrahGroupDto>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    private UmrahGroupDto toDto(UmrahGroup e) {
        return UmrahGroupDto.builder()
                .id(e.getId())
                .agencyId(e.getAgencyId())
                .name(e.getName())
                .description(e.getDescription())
                .departureDate(e.getDepartureDate())
                .returnDate(e.getReturnDate())
                .maxCapacity(e.getMaxCapacity())
                .price(e.getPrice())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private PilgrimDto pilgrimToDto(Pilgrim p) {
        return PilgrimDto.builder()
                .id(p.getId())
                .agencyId(p.getAgencyId())
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .gender(p.getGender())
                .dateOfBirth(p.getDateOfBirth())
                .passportNumber(p.getPassportNumber())
                .nationality(p.getNationality())
                .phone(p.getPhone())
                .email(p.getEmail())
                .visaStatus(p.getVisaStatus())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
