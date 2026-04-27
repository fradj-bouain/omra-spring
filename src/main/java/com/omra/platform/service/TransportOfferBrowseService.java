package com.omra.platform.service;

import com.omra.platform.dto.TransportOfferDto;
import com.omra.platform.entity.Agency;
import com.omra.platform.entity.TransportOffer;
import com.omra.platform.entity.TransportVehicle;
import com.omra.platform.entity.enums.AgencyKind;
import com.omra.platform.entity.enums.TransportOfferStatus;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.AgencyRepository;
import com.omra.platform.repository.TransportOfferRepository;
import com.omra.platform.repository.TransportVehicleRepository;
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
public class TransportOfferBrowseService {

    private final AgencyRepository agencyRepository;
    private final TransportOfferRepository offerRepository;
    private final TransportVehicleRepository vehicleRepository;

    @Transactional(readOnly = true)
    public List<TransportOfferDto> listActiveOffersForTravelAgencies() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null) {
            throw new ForbiddenException("Agence requise.");
        }
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));
        if (agency.getAgencyKind() != AgencyKind.TRAVEL) {
            throw new ForbiddenException("Réservé aux agences de type voyage.");
        }
        List<TransportOffer> offers = offerRepository.findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(TransportOfferStatus.ACTIVE);
        Map<Long, TransportVehicle> vehicles = loadVehiclesById(offers);
        Map<Long, Agency> transportAgencies = loadAgenciesById(offers);
        return offers.stream()
                .map(o -> toDto(o, vehicles.get(o.getVehicleId()), transportAgencies.get(o.getAgencyId())))
                .collect(Collectors.toList());
    }

    private Map<Long, TransportVehicle> loadVehiclesById(List<TransportOffer> offers) {
        Set<Long> ids = new HashSet<>();
        for (TransportOffer o : offers) {
            if (o.getVehicleId() != null) ids.add(o.getVehicleId());
        }
        if (ids.isEmpty()) return Map.of();
        Map<Long, TransportVehicle> map = new HashMap<>();
        for (TransportVehicle v : vehicleRepository.findAllById(ids)) {
            map.put(v.getId(), v);
        }
        return map;
    }

    private Map<Long, Agency> loadAgenciesById(List<TransportOffer> offers) {
        Set<Long> ids = new HashSet<>();
        for (TransportOffer o : offers) {
            if (o.getAgencyId() != null) ids.add(o.getAgencyId());
        }
        if (ids.isEmpty()) return Map.of();
        Map<Long, Agency> map = new HashMap<>();
        for (Agency a : agencyRepository.findAllById(ids)) {
            map.put(a.getId(), a);
        }
        return map;
    }

    private TransportOfferDto toDto(TransportOffer o, TransportVehicle v, Agency transportAgency) {
        return TransportOfferDto.builder()
                .id(o.getId())
                .vehicleId(o.getVehicleId())
                .transportAgencyId(o.getAgencyId())
                .transportAgencyName(transportAgency != null ? transportAgency.getName() : null)
                .vehicleType(v != null ? v.getVehicleType() : null)
                .vehicleSeatCount(v != null ? v.getSeatCount() : null)
                .vehiclePlate(v != null ? v.getPlate() : null)
                .vehicleBrand(v != null ? v.getBrand() : null)
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
