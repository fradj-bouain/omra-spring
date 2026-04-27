package com.omra.platform.service;

import com.omra.platform.dto.HotelReservationCreateDto;
import com.omra.platform.dto.TransportReservationDto;
import com.omra.platform.dto.TransportReservationTravelViewDto;
import com.omra.platform.entity.Agency;
import com.omra.platform.entity.TransportOffer;
import com.omra.platform.entity.TransportOfferReservation;
import com.omra.platform.entity.TransportVehicle;
import com.omra.platform.entity.enums.AgencyKind;
import com.omra.platform.entity.enums.HotelReservationStatus;
import com.omra.platform.entity.enums.TransportOfferStatus;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.AgencyRepository;
import com.omra.platform.repository.TransportOfferRepository;
import com.omra.platform.repository.TransportOfferReservationRepository;
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
public class TransportReservationService {

    private final AgencyRepository agencyRepository;
    private final TransportOfferRepository offerRepository;
    private final TransportOfferReservationRepository reservationRepository;
    private final TransportVehicleRepository vehicleRepository;

    @Transactional
    public TransportReservationDto createReservation(Long offerId, HotelReservationCreateDto dto) {
        Long travelAgencyId = requireAgencyIdOfKind(AgencyKind.TRAVEL);
        TransportOffer offer = offerRepository.findByIdAndDeletedAtIsNull(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("TransportOffer", offerId));
        if (offer.getStatus() != TransportOfferStatus.ACTIVE) {
            throw new BadRequestException("Cette offre n'est pas active.");
        }
        if (dto.getContactName() == null || dto.getContactName().isBlank()) {
            throw new BadRequestException("contactName est requis.");
        }
        if (dto.getDesiredFrom() != null && dto.getDesiredTo() != null && dto.getDesiredTo().isBefore(dto.getDesiredFrom())) {
            throw new BadRequestException("desiredTo doit être après desiredFrom.");
        }

        Integer units = dto.getUnits();
        Integer minU = offer.getMinUnits();
        Integer maxU = offer.getMaxUnits();
        if (minU != null || maxU != null) {
            if (units == null) {
                throw new BadRequestException("Indiquez le nombre de passagers / unités selon les bornes de l'offre.");
            }
            if (minU != null && units < minU) {
                throw new BadRequestException("Le nombre d'unités ne peut pas être inférieur au minimum de l'offre (" + minU + ").");
            }
            if (maxU != null && units > maxU) {
                throw new BadRequestException("Le nombre d'unités ne peut pas dépasser le maximum de l'offre (" + maxU + ").");
            }
        } else if (units != null && units < 1) {
            throw new BadRequestException("Le nombre d'unités doit être au moins 1.");
        }

        TransportOfferReservation r = TransportOfferReservation.builder()
                .offerId(offerId)
                .transportAgencyId(offer.getAgencyId())
                .travelAgencyId(travelAgencyId)
                .status(HotelReservationStatus.PENDING)
                .contactName(dto.getContactName().trim())
                .contactPhone(blankToNull(dto.getContactPhone()))
                .contactEmail(blankToNull(dto.getContactEmail()))
                .units(dto.getUnits())
                .desiredFrom(dto.getDesiredFrom())
                .desiredTo(dto.getDesiredTo())
                .note(blankToNull(dto.getNote()))
                .build();
        return toDto(reservationRepository.save(r));
    }

    @Transactional(readOnly = true)
    public List<TransportReservationDto> listIncomingReservations() {
        Long transportAgencyId = requireAgencyIdOfKind(AgencyKind.TRANSPORT);
        return reservationRepository.findByTransportAgencyIdOrderByCreatedAtDesc(transportAgencyId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransportReservationTravelViewDto> listReservationsForTravelAgency() {
        Long travelAgencyId = requireAgencyIdOfKind(AgencyKind.TRAVEL);
        List<TransportOfferReservation> list =
                reservationRepository.findByTravelAgencyIdOrderByCreatedAtDesc(travelAgencyId);
        if (list.isEmpty()) {
            return List.of();
        }
        Set<Long> offerIds = new HashSet<>();
        Set<Long> transportAgencyIds = new HashSet<>();
        for (TransportOfferReservation r : list) {
            offerIds.add(r.getOfferId());
            transportAgencyIds.add(r.getTransportAgencyId());
        }
        Map<Long, TransportOffer> offerMap = new HashMap<>();
        for (TransportOffer o : offerRepository.findAllById(offerIds)) {
            offerMap.put(o.getId(), o);
        }
        Set<Long> vehicleIds = new HashSet<>();
        for (TransportOffer o : offerMap.values()) {
            if (o.getVehicleId() != null) vehicleIds.add(o.getVehicleId());
        }
        Map<Long, TransportVehicle> vehicleMap = new HashMap<>();
        for (TransportVehicle v : vehicleRepository.findAllById(vehicleIds)) {
            vehicleMap.put(v.getId(), v);
        }
        Map<Long, Agency> transportAgencyMap = new HashMap<>();
        for (Agency a : agencyRepository.findAllById(transportAgencyIds)) {
            transportAgencyMap.put(a.getId(), a);
        }
        return list.stream()
                .map(r -> toTravelView(r, offerMap.get(r.getOfferId()), vehicleMap, transportAgencyMap))
                .collect(Collectors.toList());
    }

    @Transactional
    public TransportReservationDto setReservationStatus(Long reservationId, HotelReservationStatus status) {
        Long transportAgencyId = requireAgencyIdOfKind(AgencyKind.TRANSPORT);
        TransportOfferReservation r = reservationRepository.findByIdAndTransportAgencyId(reservationId, transportAgencyId)
                .orElseThrow(() -> new ResourceNotFoundException("TransportOfferReservation", reservationId));
        r.setStatus(status);
        return toDto(reservationRepository.save(r));
    }

    private Long requireAgencyIdOfKind(AgencyKind kind) {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null) {
            throw new ForbiddenException("Agence requise.");
        }
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));
        if (agency.getAgencyKind() != kind) {
            throw new ForbiddenException("Réservé aux agences de type " + kind.name() + ".");
        }
        return agencyId;
    }

    private String blankToNull(String s) {
        if (s == null || s.isBlank()) return null;
        return s.trim();
    }

    private TransportReservationDto toDto(TransportOfferReservation r) {
        return TransportReservationDto.builder()
                .id(r.getId())
                .offerId(r.getOfferId())
                .transportAgencyId(r.getTransportAgencyId())
                .travelAgencyId(r.getTravelAgencyId())
                .status(r.getStatus())
                .contactName(r.getContactName())
                .contactPhone(r.getContactPhone())
                .contactEmail(r.getContactEmail())
                .units(r.getUnits())
                .desiredFrom(r.getDesiredFrom())
                .desiredTo(r.getDesiredTo())
                .note(r.getNote())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private TransportReservationTravelViewDto toTravelView(
            TransportOfferReservation r,
            TransportOffer offer,
            Map<Long, TransportVehicle> vehicleMap,
            Map<Long, Agency> transportAgencyMap) {
        TransportVehicle v = offer != null && offer.getVehicleId() != null
                ? vehicleMap.get(offer.getVehicleId())
                : null;
        Agency ta = transportAgencyMap.get(r.getTransportAgencyId());
        return TransportReservationTravelViewDto.builder()
                .id(r.getId())
                .offerId(r.getOfferId())
                .offerTitle(offer != null ? offer.getTitle() : null)
                .vehiclePlate(v != null ? v.getPlate() : null)
                .transportAgencyName(ta != null ? ta.getName() : null)
                .status(r.getStatus())
                .contactName(r.getContactName())
                .contactPhone(r.getContactPhone())
                .contactEmail(r.getContactEmail())
                .units(r.getUnits())
                .desiredFrom(r.getDesiredFrom())
                .desiredTo(r.getDesiredTo())
                .note(r.getNote())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
