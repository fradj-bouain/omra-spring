package com.omra.platform.repository;

import com.omra.platform.entity.GroupHotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupHotelRepository extends JpaRepository<GroupHotel, Long> {

    List<GroupHotel> findByGroupId(Long groupId);
}
