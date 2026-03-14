package com.omra.platform.service;

import com.omra.platform.dto.FlightSeatAssignmentDto;
import com.omra.platform.dto.FlightSeatDto;
import com.omra.platform.entity.Flight;
import com.omra.platform.entity.FlightSeat;
import com.omra.platform.entity.FlightSeatAssignment;
import com.omra.platform.entity.enums.SeatStatus;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.FlightRepository;
import com.omra.platform.repository.FlightSeatAssignmentRepository;
import com.omra.platform.repository.FlightSeatRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlightSeatService {

    private final FlightSeatRepository flightSeatRepository;
    private final FlightSeatAssignmentRepository flightSeatAssignmentRepository;
    private final FlightRepository flightRepository;

    private void ensureFlightAccess(Long flightId) {
        Flight f = flightRepository.findById(flightId).orElseThrow(() -> new ResourceNotFoundException("Flight", flightId));
        Long agencyId = TenantContext.getAgencyId();
        if (!TenantContext.isSuperAdmin() && (agencyId == null || !agencyId.equals(f.getAgencyId())))
            throw new ForbiddenException("Access denied to this flight");
        if (f.getDeletedAt() != null) throw new ResourceNotFoundException("Flight", flightId);
    }

    @Transactional(readOnly = true)
    public List<FlightSeatDto> getByFlight(Long flightId) {
        ensureFlightAccess(flightId);
        List<FlightSeat> seats = flightSeatRepository.findByFlightId(flightId);
        return seats.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public List<FlightSeatDto> createSeats(Long flightId, List<String> seatNumbers) {
        ensureFlightAccess(flightId);
        for (String sn : seatNumbers) {
            if (flightSeatRepository.findByFlightId(flightId).stream().anyMatch(s -> sn.equals(s.getSeatNumber())))
                continue;
            FlightSeat seat = FlightSeat.builder()
                    .flightId(flightId)
                    .seatNumber(sn)
                    .status(SeatStatus.AVAILABLE)
                    .build();
            flightSeatRepository.save(seat);
        }
        return flightSeatRepository.findByFlightId(flightId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public FlightSeatAssignmentDto assignPilgrim(Long flightId, Long flightSeatId, Long pilgrimId) {
        ensureFlightAccess(flightId);
        FlightSeat seat = flightSeatRepository.findById(flightSeatId).orElseThrow(() -> new ResourceNotFoundException("FlightSeat", flightSeatId));
        if (!seat.getFlightId().equals(flightId)) throw new BadRequestException("Seat does not belong to this flight");
        if (seat.getStatus() == SeatStatus.OCCUPIED)
            throw new BadRequestException("Seat already occupied");
        if (flightSeatAssignmentRepository.existsByFlightIdAndPilgrimId(flightId, pilgrimId))
            throw new BadRequestException("Pilgrim already has a seat on this flight");
        FlightSeatAssignment a = FlightSeatAssignment.builder()
                .flightId(flightId)
                .flightSeatId(flightSeatId)
                .pilgrimId(pilgrimId)
                .build();
        a = flightSeatAssignmentRepository.save(a);
        seat.setStatus(SeatStatus.OCCUPIED);
        flightSeatRepository.save(seat);
        return toAssignmentDto(a);
    }

    @Transactional(readOnly = true)
    public List<FlightSeatAssignmentDto> getAssignmentsByFlight(Long flightId) {
        ensureFlightAccess(flightId);
        return flightSeatAssignmentRepository.findByFlightId(flightId).stream().map(this::toAssignmentDto).collect(Collectors.toList());
    }

    private FlightSeatDto toDto(FlightSeat e) {
        Long pilgrimId = flightSeatAssignmentRepository.findByFlightSeatId(e.getId())
                .map(FlightSeatAssignment::getPilgrimId)
                .orElse(null);
        return FlightSeatDto.builder()
                .id(e.getId())
                .flightId(e.getFlightId())
                .seatNumber(e.getSeatNumber())
                .status(e.getStatus())
                .pilgrimId(pilgrimId)
                .createdAt(e.getCreatedAt())
                .build();
    }

    private FlightSeatAssignmentDto toAssignmentDto(FlightSeatAssignment a) {
        return FlightSeatAssignmentDto.builder()
                .id(a.getId())
                .flightId(a.getFlightId())
                .flightSeatId(a.getFlightSeatId())
                .pilgrimId(a.getPilgrimId())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
