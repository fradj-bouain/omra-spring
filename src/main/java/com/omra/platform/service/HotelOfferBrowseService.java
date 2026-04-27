package com.omra.platform.service;

import com.omra.platform.dto.HotelOfferDto;
import com.omra.platform.entity.Agency;
import com.omra.platform.entity.HotelOffer;
import com.omra.platform.entity.enums.AgencyKind;
import com.omra.platform.entity.enums.HotelOfferStatus;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.AgencyRepository;
import com.omra.platform.repository.HotelOfferRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelOfferBrowseService {

    private final AgencyRepository agencyRepository;
    private final HotelOfferRepository offerRepository;

    @Transactional(readOnly = true)
    public List<HotelOfferDto> listActiveOffersForTravelAgencies() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null) {
            throw new ForbiddenException("Agence requise.");
        }
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));
        if (agency.getAgencyKind() != AgencyKind.TRAVEL) {
            throw new ForbiddenException("Réservé aux agences de type voyage.");
        }
        return offerRepository.findByStatusOrderByCreatedAtDesc(HotelOfferStatus.ACTIVE).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private HotelOfferDto toDto(HotelOffer o) {
        return HotelOfferDto.builder()
                .id(o.getId())
                .propertyId(o.getPropertyId())
                .title(o.getTitle())
                .description(o.getDescription())
                .imageUrl(o.getImageUrl())
                .status(o.getStatus())
                .pricingUnit(o.getPricingUnit())
                .price(o.getPrice())
                .currency(o.getCurrency())
                .minUnits(o.getMinUnits())
                .maxUnits(o.getMaxUnits())
                .validFrom(o.getValidFrom())
                .validTo(o.getValidTo())
                .createdAt(o.getCreatedAt())
                .build();
    }
}

