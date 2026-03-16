package com.omra.platform.repository;

import com.omra.platform.entity.PlanningItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanningItemRepository extends JpaRepository<PlanningItem, Long> {

    List<PlanningItem> findByPlanningIdOrderBySortOrderAsc(Long planningId);

    void deleteByPlanningId(Long planningId);
}
