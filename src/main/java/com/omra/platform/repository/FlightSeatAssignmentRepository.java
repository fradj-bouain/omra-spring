package com.omra.platform.repository;

import com.omra.platform.entity.FlightSeatAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlightSeatAssignmentRepository extends JpaRepository<FlightSeatAssignment, Long> {

    List<FlightSeatAssignment> findByFlightId(Long flightId);

    Optional<FlightSeatAssignment> findByFlightIdAndPilgrimId(Long flightId, Long pilgrimId);

    boolean existsByFlightIdAndPilgrimId(Long flightId, Long pilgrimId);

    boolean existsByFlightSeatId(Long flightSeatId);

    Optional<FlightSeatAssignment> findByFlightSeatId(Long flightSeatId);
}
