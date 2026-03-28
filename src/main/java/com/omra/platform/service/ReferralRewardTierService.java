package com.omra.platform.service;

import com.omra.platform.dto.ReferralRewardTierDto;
import com.omra.platform.entity.ReferralRewardTier;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.ReferralRewardTierRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReferralRewardTierService {

    private final ReferralRewardTierRepository tierRepository;

    private Long requireAgencyId() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null) {
            throw new ForbiddenException("Agence requise");
        }
        return agencyId;
    }

    @Transactional(readOnly = true)
    public List<ReferralRewardTierDto> listForAgency() {
        Long agencyId = requireAgencyId();
        return tierRepository.findByAgencyIdAndDeletedAtIsNullOrderByPointsThresholdAscSortOrderAsc(agencyId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReferralRewardTierDto create(ReferralRewardTierDto dto) {
        Long agencyId = requireAgencyId();
        if (dto.getPointsThreshold() == null || dto.getPointsThreshold() < 1) {
            throw new BadRequestException("Seuil de points invalide");
        }
        if (dto.getGiftTitle() == null || dto.getGiftTitle().isBlank()) {
            throw new BadRequestException("Titre du cadeau requis");
        }
        if (tierRepository.existsByAgencyIdAndPointsThresholdAndDeletedAtIsNull(agencyId, dto.getPointsThreshold())) {
            throw new BadRequestException("Un palier existe déjà pour ce nombre de points");
        }
        ReferralRewardTier e = ReferralRewardTier.builder()
                .agencyId(agencyId)
                .pointsThreshold(dto.getPointsThreshold())
                .giftTitle(dto.getGiftTitle().trim())
                .giftDescription(dto.getGiftDescription() != null ? dto.getGiftDescription().trim() : null)
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .build();
        return toDto(tierRepository.save(e));
    }

    @Transactional
    public ReferralRewardTierDto update(Long id, ReferralRewardTierDto dto) {
        Long agencyId = requireAgencyId();
        ReferralRewardTier e = tierRepository.findByIdAndAgencyIdAndDeletedAtIsNull(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("ReferralRewardTier", id));
        if (dto.getGiftTitle() != null) e.setGiftTitle(dto.getGiftTitle().trim());
        if (dto.getGiftDescription() != null) e.setGiftDescription(dto.getGiftDescription().trim());
        if (dto.getSortOrder() != null) e.setSortOrder(dto.getSortOrder());
        if (dto.getPointsThreshold() != null && !dto.getPointsThreshold().equals(e.getPointsThreshold())) {
            if (tierRepository.existsByAgencyIdAndPointsThresholdAndDeletedAtIsNullAndIdNot(agencyId, dto.getPointsThreshold(), id)) {
                throw new BadRequestException("Un palier existe déjà pour ce nombre de points");
            }
            e.setPointsThreshold(dto.getPointsThreshold());
        }
        return toDto(tierRepository.save(e));
    }

    @Transactional
    public void delete(Long id) {
        Long agencyId = requireAgencyId();
        ReferralRewardTier e = tierRepository.findByIdAndAgencyIdAndDeletedAtIsNull(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("ReferralRewardTier", id));
        e.setDeletedAt(Instant.now());
        tierRepository.save(e);
    }

    private ReferralRewardTierDto toDto(ReferralRewardTier e) {
        return ReferralRewardTierDto.builder()
                .id(e.getId())
                .agencyId(e.getAgencyId())
                .pointsThreshold(e.getPointsThreshold())
                .giftTitle(e.getGiftTitle())
                .giftDescription(e.getGiftDescription())
                .sortOrder(e.getSortOrder())
                .build();
    }
}
