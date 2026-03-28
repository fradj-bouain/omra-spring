package com.omra.platform.repository;

import com.omra.platform.entity.ReferralCampaignWinner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReferralCampaignWinnerRepository extends JpaRepository<ReferralCampaignWinner, Long> {

    long countByCampaignId(Long campaignId);

    boolean existsByCampaignIdAndPilgrimId(Long campaignId, Long pilgrimId);

    List<ReferralCampaignWinner> findByCampaignIdOrderByRankOrderAsc(Long campaignId);

    @Query("SELECT COALESCE(MAX(w.rankOrder), 0) FROM ReferralCampaignWinner w WHERE w.campaignId = :campaignId")
    int findMaxRank(@Param("campaignId") Long campaignId);
}
