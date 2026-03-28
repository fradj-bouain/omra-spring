package com.omra.platform.repository;

import com.omra.platform.entity.ReferralCampaignSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReferralCampaignSlotRepository extends JpaRepository<ReferralCampaignSlot, Long> {

    List<ReferralCampaignSlot> findByCampaignIdOrderByRankOrderAsc(Long campaignId);

    Optional<ReferralCampaignSlot> findByCampaignIdAndRankOrder(Long campaignId, Integer rankOrder);
}
