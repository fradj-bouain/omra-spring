package com.omra.platform.repository;

import com.omra.platform.entity.ReferralCampaign;
import com.omra.platform.entity.enums.ReferralCampaignStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReferralCampaignRepository extends JpaRepository<ReferralCampaign, Long> {

    @Query("SELECT c.id FROM ReferralCampaign c WHERE c.agencyId = :agencyId AND c.status = :st "
            + "AND c.startsAt <= :now AND c.endsAt >= :now ORDER BY c.id ASC")
    Optional<Long> findFirstOpenCampaignId(
            @Param("agencyId") Long agencyId,
            @Param("st") ReferralCampaignStatus status,
            @Param("now") Instant now);

    List<ReferralCampaign> findByAgencyIdOrderByCreatedAtDesc(Long agencyId);

    List<ReferralCampaign> findByAgencyIdAndStatus(Long agencyId, ReferralCampaignStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ReferralCampaign c WHERE c.id = :id")
    Optional<ReferralCampaign> findByIdForUpdate(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ReferralCampaign c SET c.status = :closed, c.closedAt = :ts WHERE c.status = :active AND c.endsAt < :ts")
    int closeWhereEnded(@Param("active") ReferralCampaignStatus active,
                        @Param("closed") ReferralCampaignStatus closed,
                        @Param("ts") Instant ts);
}
