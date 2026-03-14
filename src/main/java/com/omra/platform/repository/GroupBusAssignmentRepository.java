package com.omra.platform.repository;

import com.omra.platform.entity.GroupBusAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupBusAssignmentRepository extends JpaRepository<GroupBusAssignment, Long> {

    List<GroupBusAssignment> findByGroupId(Long groupId);

    Optional<GroupBusAssignment> findByGroupIdAndBusId(Long groupId, Long busId);

    boolean existsByGroupIdAndBusId(Long groupId, Long busId);
}
