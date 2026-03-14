package com.omra.platform.repository;

import com.omra.platform.entity.FlightSeat;
import com.omra.platform.entity.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlightSeatRepository extends JpaRepository<FlightSeat, Long> {

    List<FlightSeat> findByFlightId(Long flightId);

    long countByFlightIdAndStatus(Long flightId, SeatStatus status);
}
