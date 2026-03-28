package com.omra.platform.repository;

import com.omra.platform.entity.ReferralRewardTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReferralRewardTierRepository extends JpaRepository<ReferralRewardTier, Long> {

    List<ReferralRewardTier> findByAgencyIdAndDeletedAtIsNullOrderByPointsThresholdAscSortOrderAsc(Long agencyId);

    Optional<ReferralRewardTier> findByIdAndAgencyIdAndDeletedAtIsNull(Long id, Long agencyId);

    boolean existsByAgencyIdAndPointsThresholdAndDeletedAtIsNull(Long agencyId, Integer pointsThreshold);

    boolean existsByAgencyIdAndPointsThresholdAndDeletedAtIsNullAndIdNot(Long agencyId, Integer pointsThreshold, Long excludeId);
}
