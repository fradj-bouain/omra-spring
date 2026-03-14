package com.omra.platform.repository;

import com.omra.platform.entity.TripCostItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface TripCostItemRepository extends JpaRepository<TripCostItem, Long> {

    List<TripCostItem> findByGroupIdAndDeletedAtIsNull(Long groupId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TripCostItem t WHERE t.groupId = :groupId AND t.deletedAt IS NULL")
    BigDecimal sumAmountByGroupId(@Param("groupId") Long groupId);
}
