package com.omra.platform.service;

import com.omra.platform.dto.BusDto;
import com.omra.platform.dto.PageResponse;
import com.omra.platform.entity.Bus;
import com.omra.platform.entity.BusSeat;
import com.omra.platform.entity.GroupBusAssignment;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.entity.UmrahGroup;
import com.omra.platform.repository.BusRepository;
import com.omra.platform.repository.BusSeatRepository;
import com.omra.platform.repository.GroupBusAssignmentRepository;
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
public class BusService {

    private final BusRepository busRepository;
    private final BusSeatRepository busSeatRepository;
    private final GroupBusAssignmentRepository groupBusAssignmentRepository;
    private final UmrahGroupRepository umrahGroupRepository;

    private Long requireAgencyId() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null && !TenantContext.isSuperAdmin()) throw new ForbiddenException("Agency context required");
        return agencyId;
    }

    @Transactional(readOnly = true)
    public PageResponse<BusDto> getBuses(Pageable pageable) {
        Long agencyId = requireAgencyId();
        Page<Bus> page = busRepository.findByAgencyIdAndDeletedAtIsNull(agencyId, pageable);
        List<BusDto> content = page.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return PageResponse.<BusDto>builder()
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
    public BusDto getById(Long id) {
        Bus bus = findByIdAndAgency(id);
        return toDto(bus);
    }

    @Transactional
    public BusDto create(BusDto dto) {
        Long agencyId = requireAgencyId();
        if (agencyId == null) {
            throw new ForbiddenException("Un contexte agence est requis pour créer un bus. Connectez-vous en tant qu'utilisateur d'une agence.");
        }
        if (dto.getPlate() == null || dto.getPlate().isBlank()) {
            throw new BadRequestException("L'immatriculation est requise.");
        }
        Bus bus = Bus.builder()
                .agencyId(agencyId)
                .plate(dto.getPlate().trim())
                .capacity(dto.getCapacity() != null ? dto.getCapacity() : 50)
                .driverName(dto.getDriverName() != null ? dto.getDriverName().trim() : null)
                .driverContact(dto.getDriverContact() != null ? dto.getDriverContact().trim() : null)
                .build();
        bus = busRepository.save(bus);
        return toDto(bus);
    }

    @Transactional
    public BusDto update(Long id, BusDto dto) {
        Bus bus = findByIdAndAgency(id);
        if (dto.getPlate() != null) bus.setPlate(dto.getPlate());
        if (dto.getCapacity() != null) bus.setCapacity(dto.getCapacity());
        if (dto.getDriverName() != null) bus.setDriverName(dto.getDriverName().trim());
        if (dto.getDriverContact() != null) bus.setDriverContact(dto.getDriverContact().trim());
        bus = busRepository.save(bus);
        return toDto(bus);
    }

    @Transactional
    public void delete(Long id) {
        Bus bus = findByIdAndAgency(id);
        bus.setDeletedAt(Instant.now());
        busRepository.save(bus);
    }

    @Transactional(readOnly = true)
    public List<String> getSeatNumbers(Long busId) {
        Bus bus = findByIdAndAgency(busId);
        return busSeatRepository.findByBusId(bus.getId()).stream().map(BusSeat::getSeatNumber).sorted().collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BusDto> getBusesByGroup(Long groupId) {
        UmrahGroup group = umrahGroupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("Group", groupId));
        Long agencyId = TenantContext.getAgencyId();
        if (!TenantContext.isSuperAdmin() && (agencyId == null || !agencyId.equals(group.getAgencyId())))
            throw new ForbiddenException("Access denied to this group");
        List<GroupBusAssignment> assignments = groupBusAssignmentRepository.findByGroupId(groupId);
        if (assignments.isEmpty()) return List.of();
        List<Long> busIds = assignments.stream().map(GroupBusAssignment::getBusId).collect(Collectors.toList());
        List<Bus> buses = busRepository.findAllById(busIds);
        return buses.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public void assignBusToGroup(Long groupId, Long busId) {
        Long agencyId = requireAgencyId();
        UmrahGroup group = umrahGroupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("Group", groupId));
        if (!TenantContext.isSuperAdmin() && (agencyId == null || !agencyId.equals(group.getAgencyId())))
            throw new ForbiddenException("Access denied to this group");
        if (groupBusAssignmentRepository.existsByGroupIdAndBusId(groupId, busId)) return;
        Bus bus = findByIdAndAgency(busId);
        GroupBusAssignment a = GroupBusAssignment.builder()
                .groupId(groupId)
                .busId(bus.getId())
                .build();
        groupBusAssignmentRepository.save(a);
    }

    private Bus findByIdAndAgency(Long id) {
        Bus bus = busRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bus", id));
        Long agencyId = TenantContext.getAgencyId();
        if (!TenantContext.isSuperAdmin() && (agencyId == null || !agencyId.equals(bus.getAgencyId())))
            throw new ForbiddenException("Access denied");
        if (bus.getDeletedAt() != null) throw new ResourceNotFoundException("Bus", id);
        return bus;
    }

    private BusDto toDto(Bus e) {
        return BusDto.builder()
                .id(e.getId())
                .agencyId(e.getAgencyId())
                .plate(e.getPlate())
                .capacity(e.getCapacity())
                .driverName(e.getDriverName())
                .driverContact(e.getDriverContact())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
