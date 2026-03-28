package com.omra.platform.service;

import com.omra.platform.dto.*;
import com.omra.platform.entity.Pilgrim;
import com.omra.platform.entity.ReferralCampaign;
import com.omra.platform.entity.ReferralCampaignSlot;
import com.omra.platform.entity.ReferralCampaignWinner;
import com.omra.platform.entity.ReferralRewardTier;
import com.omra.platform.entity.enums.ReferralCampaignStatus;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.PilgrimRepository;
import com.omra.platform.repository.ReferralCampaignRepository;
import com.omra.platform.repository.ReferralCampaignSlotRepository;
import com.omra.platform.repository.ReferralCampaignWinnerRepository;
import com.omra.platform.repository.ReferralRewardTierRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReferralCampaignService {

    private final ReferralCampaignRepository campaignRepository;
    private final ReferralCampaignSlotRepository slotRepository;
    private final ReferralCampaignWinnerRepository winnerRepository;
    private final ReferralRewardTierRepository tierRepository;
    private final PilgrimRepository pilgrimRepository;

    private Long requireAgencyId() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null) {
            throw new ForbiddenException("Agence requise");
        }
        return agencyId;
    }

    /** Fermeture automatique des campagnes ACTIVE dépassées (scheduler + avant attribution). */
    @Transactional
    public int closeExpiredCampaigns() {
        Instant now = Instant.now();
        return campaignRepository.closeWhereEnded(ReferralCampaignStatus.ACTIVE, ReferralCampaignStatus.CLOSED, now);
    }

    @Transactional
    public ReferralCampaignDashboardDto getDashboard() {
        Long agencyId = requireAgencyId();
        closeExpiredCampaigns();
        List<ReferralCampaign> activeList = campaignRepository.findByAgencyIdAndStatus(agencyId, ReferralCampaignStatus.ACTIVE);
        if (activeList.isEmpty()) {
            return ReferralCampaignDashboardDto.builder()
                    .idle(true)
                    .phase("IDLE")
                    .winnersCount(0)
                    .winners(List.of())
                    .slots(List.of())
                    .build();
        }
        ReferralCampaign c = activeList.get(0);
        List<ReferralCampaignSlotDto> slotDtos = buildSlotDtos(c.getId(), agencyId);
        List<ReferralCampaignWinnerRowDto> winners = loadWinnerRows(c.getId(), agencyId);
        int wc = winners.size();
        Instant now = Instant.now();
        String phase = resolvePhase(c, now, wc);

        return ReferralCampaignDashboardDto.builder()
                .idle(false)
                .campaignId(c.getId())
                .title(c.getTitle())
                .status(c.getStatus().name())
                .startsAt(c.getStartsAt())
                .endsAt(c.getEndsAt())
                .maxWinners(c.getMaxWinners())
                .winnersCount(wc)
                .phase(phase)
                .slots(slotDtos)
                .winners(winners)
                .build();
    }

    private List<ReferralCampaignSlotDto> buildSlotDtos(Long campaignId, Long agencyId) {
        return slotRepository.findByCampaignIdOrderByRankOrderAsc(campaignId).stream()
                .map(s -> {
                    ReferralRewardTier t = tierRepository
                            .findByIdAndAgencyIdAndDeletedAtIsNull(s.getRewardTierId(), agencyId)
                            .orElse(null);
                    return ReferralCampaignSlotDto.builder()
                            .rankOrder(s.getRankOrder())
                            .rewardTierId(s.getRewardTierId())
                            .pointsThreshold(t != null ? t.getPointsThreshold() : null)
                            .giftTitle(t != null ? t.getGiftTitle() : null)
                            .giftDescription(t != null ? t.getGiftDescription() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String resolvePhase(ReferralCampaign c, Instant now, int winnersCount) {
        if (c.getStatus() == ReferralCampaignStatus.CLOSED) {
            if (winnersCount >= c.getMaxWinners()) {
                return "ENDED_FULL";
            }
            return "ENDED_TIME";
        }
        if (now.isBefore(c.getStartsAt())) {
            return "UPCOMING";
        }
        if (now.isAfter(c.getEndsAt())) {
            return "ENDED_TIME";
        }
        if (winnersCount >= c.getMaxWinners()) {
            return "ENDED_FULL";
        }
        return "LIVE";
    }

    private List<ReferralCampaignWinnerRowDto> loadWinnerRows(Long campaignId, Long agencyId) {
        return winnerRepository.findByCampaignIdOrderByRankOrderAsc(campaignId).stream()
                .map(w -> {
                    String name = pilgrimRepository.findById(w.getPilgrimId())
                            .filter(p -> p.getDeletedAt() == null)
                            .map(p -> p.getFirstName() + " " + p.getLastName())
                            .orElse("#" + w.getPilgrimId());
                    ReferralRewardTier giftTier = null;
                    if (w.getRewardTierId() != null) {
                        giftTier = tierRepository
                                .findByIdAndAgencyIdAndDeletedAtIsNull(w.getRewardTierId(), agencyId)
                                .orElse(null);
                    }
                    return ReferralCampaignWinnerRowDto.builder()
                            .pilgrimId(w.getPilgrimId())
                            .pilgrimDisplayName(name)
                            .rankOrder(w.getRankOrder())
                            .wonAt(w.getWonAt())
                            .pointsAtWin(w.getPointsAtWin())
                            .rewardTierId(w.getRewardTierId())
                            .giftTitle(giftTier != null ? giftTier.getGiftTitle() : null)
                            .giftDescription(giftTier != null ? giftTier.getGiftDescription() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReferralCampaignResponseDto> listForAgency() {
        Long agencyId = requireAgencyId();
        return campaignRepository.findByAgencyIdOrderByCreatedAtDesc(agencyId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReferralCampaignResponseDto create(ReferralCampaignCreateRequest req) {
        Long agencyId = requireAgencyId();
        validateRequest(req);
        List<Long> tierIds = req.getSlotRewardTierIds();
        int maxWinners = tierIds.size();
        for (Long tid : tierIds) {
            tierRepository.findByIdAndAgencyIdAndDeletedAtIsNull(tid, agencyId)
                    .orElseThrow(() -> new BadRequestException("Palier inconnu pour cette agence (rang invalide)"));
        }
        ReferralCampaign c = ReferralCampaign.builder()
                .agencyId(agencyId)
                .title(trimOrNull(req.getTitle()))
                .startsAt(req.getStartsAt())
                .endsAt(req.getEndsAt())
                .maxWinners(maxWinners)
                .status(ReferralCampaignStatus.DRAFT)
                .build();
        c = campaignRepository.save(c);
        for (int i = 0; i < tierIds.size(); i++) {
            slotRepository.save(ReferralCampaignSlot.builder()
                    .campaignId(c.getId())
                    .rankOrder(i + 1)
                    .rewardTierId(tierIds.get(i))
                    .build());
        }
        return toResponse(c);
    }

    @Transactional
    public ReferralCampaignResponseDto activate(Long id) {
        Long agencyId = requireAgencyId();
        ReferralCampaign c = campaignRepository.findById(id)
                .filter(x -> x.getAgencyId().equals(agencyId))
                .orElseThrow(() -> new ResourceNotFoundException("ReferralCampaign", id));
        if (c.getStatus() == ReferralCampaignStatus.CLOSED) {
            throw new BadRequestException("Cette campagne est déjà clôturée");
        }
        Instant now = Instant.now();
        for (ReferralCampaign other : campaignRepository.findByAgencyIdAndStatus(agencyId, ReferralCampaignStatus.ACTIVE)) {
            if (!other.getId().equals(id)) {
                other.setStatus(ReferralCampaignStatus.CLOSED);
                other.setClosedAt(now);
                campaignRepository.save(other);
            }
        }
        c.setStatus(ReferralCampaignStatus.ACTIVE);
        return toResponse(campaignRepository.save(c));
    }

    @Transactional
    public ReferralCampaignResponseDto closeManual(Long id) {
        Long agencyId = requireAgencyId();
        ReferralCampaign c = campaignRepository.findById(id)
                .filter(x -> x.getAgencyId().equals(agencyId))
                .orElseThrow(() -> new ResourceNotFoundException("ReferralCampaign", id));
        if (c.getStatus() != ReferralCampaignStatus.ACTIVE) {
            throw new BadRequestException("Seule une campagne active peut être fermée manuellement ainsi");
        }
        c.setStatus(ReferralCampaignStatus.CLOSED);
        c.setClosedAt(Instant.now());
        return toResponse(campaignRepository.save(c));
    }

    /**
     * Appelé après crédit des points au parrain : premier arrivé, premier servi, dans la fenêtre ACTIVE.
     * Le palier (seuil + cadeau) dépend du prochain rang disponible.
     */
    @Transactional
    public void tryAwardWinner(Long referrerPilgrimId) {
        closeExpiredCampaigns();
        Pilgrim ref = pilgrimRepository.findById(referrerPilgrimId).orElse(null);
        if (ref == null || ref.getDeletedAt() != null) {
            return;
        }
        Instant now = Instant.now();
        Optional<Long> campaignIdOpt = campaignRepository.findFirstOpenCampaignId(
                ref.getAgencyId(), ReferralCampaignStatus.ACTIVE, now);
        if (campaignIdOpt.isEmpty()) {
            return;
        }
        ReferralCampaign campaign = campaignRepository.findByIdForUpdate(campaignIdOpt.get()).orElse(null);
        if (campaign == null || campaign.getStatus() != ReferralCampaignStatus.ACTIVE) {
            return;
        }
        if (now.isBefore(campaign.getStartsAt()) || now.isAfter(campaign.getEndsAt())) {
            return;
        }
        if (winnerRepository.existsByCampaignIdAndPilgrimId(campaign.getId(), ref.getId())) {
            return;
        }
        long wc = winnerRepository.countByCampaignId(campaign.getId());
        if (wc >= campaign.getMaxWinners()) {
            return;
        }
        int nextRank = winnerRepository.findMaxRank(campaign.getId()) + 1;
        ReferralCampaignSlot slot = slotRepository
                .findByCampaignIdAndRankOrder(campaign.getId(), nextRank)
                .orElse(null);
        if (slot == null) {
            return;
        }
        ReferralRewardTier tier = tierRepository
                .findByIdAndAgencyIdAndDeletedAtIsNull(slot.getRewardTierId(), ref.getAgencyId())
                .orElse(null);
        if (tier == null) {
            return;
        }
        int pts = ref.getReferralPoints() == null ? 0 : ref.getReferralPoints();
        if (pts < tier.getPointsThreshold()) {
            return;
        }
        ReferralCampaignWinner winner = ReferralCampaignWinner.builder()
                .campaignId(campaign.getId())
                .pilgrimId(ref.getId())
                .rankOrder(nextRank)
                .wonAt(now)
                .pointsAtWin(pts)
                .rewardTierId(slot.getRewardTierId())
                .build();
        winnerRepository.save(winner);
        if (wc + 1 >= campaign.getMaxWinners()) {
            campaign.setStatus(ReferralCampaignStatus.CLOSED);
            campaign.setClosedAt(now);
            campaignRepository.save(campaign);
        }
    }

    private void validateRequest(ReferralCampaignCreateRequest req) {
        if (req.getStartsAt() == null || req.getEndsAt() == null) {
            throw new BadRequestException("Dates de début et fin requises");
        }
        if (!req.getEndsAt().isAfter(req.getStartsAt())) {
            throw new BadRequestException("La fin doit être après le début");
        }
        List<Long> ids = req.getSlotRewardTierIds();
        if (ids == null || ids.isEmpty() || ids.size() > 500) {
            throw new BadRequestException("Indiquez entre 1 et 500 places, chacune avec un palier / cadeau");
        }
        for (Long tid : ids) {
            if (tid == null) {
                throw new BadRequestException("Chaque place doit référencer un palier valide");
            }
        }
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private ReferralCampaignResponseDto toResponse(ReferralCampaign c) {
        List<Long> tierIds = slotRepository.findByCampaignIdOrderByRankOrderAsc(c.getId()).stream()
                .map(ReferralCampaignSlot::getRewardTierId)
                .collect(Collectors.toCollection(ArrayList::new));
        return ReferralCampaignResponseDto.builder()
                .id(c.getId())
                .agencyId(c.getAgencyId())
                .title(c.getTitle())
                .startsAt(c.getStartsAt())
                .endsAt(c.getEndsAt())
                .slotRewardTierIds(tierIds)
                .maxWinners(c.getMaxWinners())
                .status(c.getStatus().name())
                .createdAt(c.getCreatedAt())
                .closedAt(c.getClosedAt())
                .build();
    }
}
