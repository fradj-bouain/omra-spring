package com.omra.platform.repository;

import com.omra.platform.entity.Planning;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanningRepository extends JpaRepository<Planning, Long> {

    List<Planning> findByAgencyIdAndDeletedAtIsNullOrderByName(Long agencyId);
}
