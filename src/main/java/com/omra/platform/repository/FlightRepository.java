package com.omra.platform.repository;

import com.omra.platform.entity.Flight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    Page<Flight> findByAgencyIdAndDeletedAtIsNull(Long agencyId, Pageable pageable);

    List<Flight> findByGroupIdAndDeletedAtIsNull(Long groupId);
}
