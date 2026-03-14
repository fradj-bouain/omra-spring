package com.omra.platform.repository;

import com.omra.platform.entity.BusSeatAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusSeatAssignmentRepository extends JpaRepository<BusSeatAssignment, Long> {

    List<BusSeatAssignment> findByGroupBusAssignmentId(Long groupBusAssignmentId);

    boolean existsByGroupBusAssignmentIdAndPilgrimId(Long groupBusAssignmentId, Long pilgrimId);

    boolean existsByGroupBusAssignmentIdAndBusSeatId(Long groupBusAssignmentId, Long busSeatId);
}
