package com.omra.platform.service;

import com.omra.platform.dto.FlightDto;
import com.omra.platform.dto.PageResponse;
import com.omra.platform.entity.Flight;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.entity.UmrahGroup;
import com.omra.platform.repository.FlightRepository;
import com.omra.platform.repository.UmrahGroupRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;
    private final UmrahGroupRepository umrahGroupRepository;

    private Long requireAgencyId() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null && !TenantContext.isSuperAdmin()) throw new ForbiddenException("Agency context required");
        return agencyId;
    }

    @Transactional(readOnly = true)
    public PageResponse<FlightDto> getFlights(Pageable pageable) {
        Long agencyId = requireAgencyId();
        Page<Flight> page = flightRepository.findByAgencyIdAndDeletedAtIsNull(agencyId, pageable);
        return toPageResponse(page);
    }

    @Transactional(readOnly = true)
    public FlightDto getById(Long id) {
        return toDto(findByIdAndAgency(id));
    }

    @Transactional(readOnly = true)
    public List<FlightDto> getByGroupId(Long groupId) {
        ensureGroupAccess(groupId);
        return flightRepository.findByGroupIdAndDeletedAtIsNull(groupId).stream().map(this::toDto).collect(Collectors.toList());
    }

    private void ensureGroupAccess(Long groupId) {
        UmrahGroup group = umrahGroupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("Group", groupId));
        Long agencyId = TenantContext.getAgencyId();
        if (!TenantContext.isSuperAdmin() && (agencyId == null || !agencyId.equals(group.getAgencyId()))) {
            throw new ForbiddenException("Access denied to this group");
        }
    }

    @Transactional
    public FlightDto create(FlightDto dto) {
        Long agencyId = requireAgencyId();
        if (agencyId == null) throw new ForbiddenException("Agency required");
        Flight flight = Flight.builder()
                .agencyId(agencyId)
                .groupId(dto.getGroupId())
                .airline(dto.getAirline())
                .flightNumber(dto.getFlightNumber())
                .departureCity(dto.getDepartureCity())
                .arrivalCity(dto.getArrivalCity())
                .departureTime(dto.getDepartureTime())
                .arrivalTime(dto.getArrivalTime())
                .terminal(dto.getTerminal())
                .gate(dto.getGate())
                .build();
        flight = flightRepository.save(flight);
        return toDto(flight);
    }

    @Transactional
    public FlightDto update(Long id, FlightDto dto) {
        Flight flight = findByIdAndAgency(id);
        if (dto.getGroupId() != null) flight.setGroupId(dto.getGroupId());
        if (dto.getAirline() != null) flight.setAirline(dto.getAirline());
        if (dto.getFlightNumber() != null) flight.setFlightNumber(dto.getFlightNumber());
        if (dto.getDepartureCity() != null) flight.setDepartureCity(dto.getDepartureCity());
        if (dto.getArrivalCity() != null) flight.setArrivalCity(dto.getArrivalCity());
        if (dto.getDepartureTime() != null) flight.setDepartureTime(dto.getDepartureTime());
        if (dto.getArrivalTime() != null) flight.setArrivalTime(dto.getArrivalTime());
        if (dto.getTerminal() != null) flight.setTerminal(dto.getTerminal());
        if (dto.getGate() != null) flight.setGate(dto.getGate());
        flight = flightRepository.save(flight);
        return toDto(flight);
    }

    private Flight findByIdAndAgency(Long id) {
        Flight flight = flightRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Flight", id));
        Long agencyId = TenantContext.getAgencyId();
        if (!TenantContext.isSuperAdmin() && (agencyId == null || !agencyId.equals(flight.getAgencyId()))) {
            throw new ForbiddenException("Access denied");
        }
        if (flight.getDeletedAt() != null) throw new ResourceNotFoundException("Flight", id);
        return flight;
    }

    private PageResponse<FlightDto> toPageResponse(Page<Flight> page) {
        List<FlightDto> content = page.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return PageResponse.<FlightDto>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    private FlightDto toDto(Flight e) {
        return FlightDto.builder()
                .id(e.getId())
                .agencyId(e.getAgencyId())
                .groupId(e.getGroupId())
                .airline(e.getAirline())
                .flightNumber(e.getFlightNumber())
                .departureCity(e.getDepartureCity())
                .arrivalCity(e.getArrivalCity())
                .departureTime(e.getDepartureTime())
                .arrivalTime(e.getArrivalTime())
                .terminal(e.getTerminal())
                .gate(e.getGate())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
