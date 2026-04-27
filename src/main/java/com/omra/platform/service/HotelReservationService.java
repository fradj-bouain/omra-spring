package com.omra.platform.service;

import com.omra.platform.dto.HotelReservationCreateDto;
import com.omra.platform.dto.HotelReservationDto;
import com.omra.platform.entity.Agency;
import com.omra.platform.entity.HotelOffer;
import com.omra.platform.entity.HotelOfferReservation;
import com.omra.platform.entity.enums.AgencyKind;
import com.omra.platform.entity.enums.HotelOfferStatus;
import com.omra.platform.entity.enums.HotelReservationStatus;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.AgencyRepository;
import com.omra.platform.repository.HotelOfferRepository;
import com.omra.platform.repository.HotelOfferReservationRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelReservationService {

    private final AgencyRepository agencyRepository;
    private final HotelOfferRepository offerRepository;
    private final HotelOfferReservationRepository reservationRepository;

    @Transactional
    public HotelReservationDto createReservation(Long offerId, HotelReservationCreateDto dto) {
        Long travelAgencyId = requireAgencyIdOfKind(AgencyKind.TRAVEL);
        HotelOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("HotelOffer", offerId));
        if (offer.getStatus() != HotelOfferStatus.ACTIVE) {
            throw new BadRequestException("Cette offre n'est pas active.");
        }
        if (dto.getContactName() == null || dto.getContactName().isBlank()) {
            throw new BadRequestException("contactName est requis.");
        }
        if (dto.getDesiredFrom() != null && dto.getDesiredTo() != null && dto.getDesiredTo().isBefore(dto.getDesiredFrom())) {
            throw new BadRequestException("desiredTo doit être après desiredFrom.");
        }

        HotelOfferReservation r = HotelOfferReservation.builder()
                .offerId(offerId)
                .hotelAgencyId(offer.getAgencyId())
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
    public List<HotelReservationDto> listIncomingReservations() {
        Long hotelAgencyId = requireAgencyIdOfKind(AgencyKind.HOTEL);
        return reservationRepository.findByHotelAgencyIdOrderByCreatedAtDesc(hotelAgencyId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public HotelReservationDto setReservationStatus(Long reservationId, HotelReservationStatus status) {
        Long hotelAgencyId = requireAgencyIdOfKind(AgencyKind.HOTEL);
        HotelOfferReservation r = reservationRepository.findByIdAndHotelAgencyId(reservationId, hotelAgencyId)
                .orElseThrow(() -> new ResourceNotFoundException("HotelOfferReservation", reservationId));
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

    private HotelReservationDto toDto(HotelOfferReservation r) {
        return HotelReservationDto.builder()
                .id(r.getId())
                .offerId(r.getOfferId())
                .hotelAgencyId(r.getHotelAgencyId())
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
}

