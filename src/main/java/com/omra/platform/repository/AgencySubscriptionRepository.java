package com.omra.platform.repository;

import com.omra.platform.entity.AgencySubscription;
import com.omra.platform.entity.enums.AgencySubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AgencySubscriptionRepository extends JpaRepository<AgencySubscription, Long> {

    Page<AgencySubscription> findByAgencyIdOrderByPeriodEndDesc(Long agencyId, Pageable pageable);

    Optional<AgencySubscription> findTopByAgencyIdOrderByIdDesc(Long agencyId);

    List<AgencySubscription> findByAgencyIdAndStatus(Long agencyId, AgencySubscriptionStatus status);

    @Query(
            """
            SELECT s FROM AgencySubscription s
            WHERE s.agencyId = :agencyId
            AND s.status = :active
            AND s.paidAt IS NOT NULL
            AND s.periodStart <= :today
            AND s.periodEnd >= :today
            ORDER BY s.periodEnd DESC
            """
    )
    List<AgencySubscription> findValidPaidCovering(
            @Param("agencyId") Long agencyId,
            @Param("active") AgencySubscriptionStatus active,
            @Param("today") LocalDate today,
            Pageable pageable);

    List<AgencySubscription> findByStatusAndPeriodEndBefore(AgencySubscriptionStatus status, LocalDate date);
}
