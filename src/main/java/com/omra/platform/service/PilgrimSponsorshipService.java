package com.omra.platform.service;

import com.omra.platform.dto.PilgrimDto;
import com.omra.platform.entity.Pilgrim;
import com.omra.platform.entity.PilgrimSponsorEvent;
import com.omra.platform.entity.ReferralRewardTier;
import com.omra.platform.entity.enums.SponsorType;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.repository.PilgrimRepository;
import com.omra.platform.repository.PilgrimSponsorEventRepository;
import com.omra.platform.repository.ReferralRewardTierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PilgrimSponsorshipService {

    private final PilgrimRepository pilgrimRepository;
    private final PilgrimSponsorEventRepository sponsorEventRepository;
    private final ReferralRewardTierRepository rewardTierRepository;
    private final ReferralCampaignService referralCampaignService;

    @Value("${app.pilgrim-sponsorship.points-per-referral:10}")
    private int pointsPerReferral;

    /**
     * Après création du pèlerin : attribue les points au parrain (autre pèlerin) si type PILGRIM et référence valide.
     */
    @Transactional
    public void afterPilgrimCreated(Pilgrim newPilgrim, PilgrimDto dto) {
        if (dto.getSponsorType() != SponsorType.PILGRIM || dto.getReferrerPilgrimId() == null) {
            return;
        }
        Long refId = dto.getReferrerPilgrimId();
        if (refId.equals(newPilgrim.getId())) {
            throw new BadRequestException("Un pèlerin ne peut pas se parrainer lui-même");
        }
        if (sponsorEventRepository.existsByReferredPilgrimId(newPilgrim.getId())) {
            return;
        }
        Pilgrim referrer = pilgrimRepository.findByIdAndAgencyIdAndDeletedAtIsNull(refId, newPilgrim.getAgencyId())
                .orElseThrow(() -> new BadRequestException("Pèlerin parrain introuvable pour cette agence"));
        PilgrimSponsorEvent event = PilgrimSponsorEvent.builder()
                .agencyId(newPilgrim.getAgencyId())
                .referrerPilgrimId(referrer.getId())
                .referredPilgrimId(newPilgrim.getId())
                .pointsAwarded(pointsPerReferral)
                .build();
        sponsorEventRepository.save(event);
        int pts = referrer.getReferralPoints() == null ? 0 : referrer.getReferralPoints();
        referrer.setReferralPoints(pts + pointsPerReferral);
        pilgrimRepository.save(referrer);
        referralCampaignService.tryAwardWinner(referrer.getId());
    }

    /** Enrichit le DTO (détail) : nom du parrain + prochain palier de cadeau. */
    public void enrichPilgrimDto(PilgrimDto dto, Pilgrim e, Long agencyId) {
        if (e.getReferrerPilgrimId() != null) {
            pilgrimRepository.findById(e.getReferrerPilgrimId())
                    .filter(p -> p.getDeletedAt() == null)
                    .ifPresent(ref -> dto.setReferrerDisplayName(
                            ref.getFirstName() + " " + ref.getLastName()));
        }
        if (agencyId != null) {
            List<ReferralRewardTier> tiers = rewardTierRepository
                    .findByAgencyIdAndDeletedAtIsNullOrderByPointsThresholdAscSortOrderAsc(agencyId);
            int pts = e.getReferralPoints() == null ? 0 : e.getReferralPoints();
            tiers.stream()
                    .filter(t -> t.getPointsThreshold() > pts)
                    .min(Comparator.comparing(ReferralRewardTier::getPointsThreshold))
                    .ifPresent(next -> {
                        dto.setNextRewardThreshold(next.getPointsThreshold());
                        dto.setNextRewardTitle(next.getGiftTitle());
                    });
        }
    }

    public int getPointsPerReferral() {
        return pointsPerReferral;
    }
}
