package com.omra.platform.service;

import com.omra.platform.dto.HotelOfferDto;
import com.omra.platform.entity.Agency;
import com.omra.platform.entity.HotelOffer;
import com.omra.platform.entity.HotelProperty;
import com.omra.platform.entity.enums.AgencyKind;
import com.omra.platform.entity.enums.HotelOfferStatus;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.AgencyRepository;
import com.omra.platform.repository.HotelOfferRepository;
import com.omra.platform.repository.HotelPropertyRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelOfferBrowseService {

    private final AgencyRepository agencyRepository;
    private final HotelOfferRepository offerRepository;
    private final HotelPropertyRepository propertyRepository;

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
        List<HotelOffer> offers = offerRepository.findByStatusOrderByCreatedAtDesc(HotelOfferStatus.ACTIVE);
        Map<Long, HotelProperty> properties = loadPropertiesById(offers);
        Map<Long, Agency> hotelAgencies = loadAgenciesById(offers);
        return offers.stream()
                .map(o -> toDto(o, properties.get(o.getPropertyId()), hotelAgencies.get(o.getAgencyId())))
                .collect(Collectors.toList());
    }

    private Map<Long, HotelProperty> loadPropertiesById(List<HotelOffer> offers) {
        Set<Long> ids = new HashSet<>();
        for (HotelOffer o : offers) {
            if (o.getPropertyId() != null) ids.add(o.getPropertyId());
        }
        if (ids.isEmpty()) return Map.of();
        Map<Long, HotelProperty> map = new HashMap<>();
        for (HotelProperty p : propertyRepository.findAllById(ids)) {
            map.put(p.getId(), p);
        }
        return map;
    }

    private Map<Long, Agency> loadAgenciesById(List<HotelOffer> offers) {
        Set<Long> ids = new HashSet<>();
        for (HotelOffer o : offers) {
            if (o.getAgencyId() != null) ids.add(o.getAgencyId());
        }
        if (ids.isEmpty()) return Map.of();
        Map<Long, Agency> map = new HashMap<>();
        for (Agency a : agencyRepository.findAllById(ids)) {
            map.put(a.getId(), a);
        }
        return map;
    }

    private HotelOfferDto toDto(HotelOffer o, HotelProperty p, Agency hotelAgency) {
        return HotelOfferDto.builder()
                .id(o.getId())
                .propertyId(o.getPropertyId())
                .hotelAgencyId(o.getAgencyId())
                .hotelAgencyName(hotelAgency != null ? hotelAgency.getName() : null)
                .propertyName(p != null ? p.getName() : null)
                .propertyCity(p != null ? p.getCity() : null)
                .propertyCountry(p != null ? p.getCountry() : null)
                .propertyAddress(p != null ? p.getAddress() : null)
                .propertyImageUrl(p != null ? p.getImageUrl() : null)
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

