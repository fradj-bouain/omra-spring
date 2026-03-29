package com.omra.platform.repository;

import com.omra.platform.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    Optional<SubscriptionPlan> findByCode(String code);

    List<SubscriptionPlan> findByActiveTrueOrderBySortOrderAscNameAsc();

    List<SubscriptionPlan> findAllByOrderBySortOrderAscNameAsc();
}
