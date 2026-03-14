package com.omra.platform.repository;

import com.omra.platform.entity.GroupRoomAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRoomAssignmentRepository extends JpaRepository<GroupRoomAssignment, Long> {

    List<GroupRoomAssignment> findByGroupHotelId(Long groupHotelId);

    List<GroupRoomAssignment> findByPilgrimId(Long pilgrimId);

    boolean existsByGroupHotelIdAndPilgrimId(Long groupHotelId, Long pilgrimId);

    boolean existsByRoomIdAndPilgrimId(Long roomId, Long pilgrimId);
}
