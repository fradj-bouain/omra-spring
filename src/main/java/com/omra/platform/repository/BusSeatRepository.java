package com.omra.platform.repository;

import com.omra.platform.entity.BusSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusSeatRepository extends JpaRepository<BusSeat, Long> {

    List<BusSeat> findByBusId(Long busId);
}
