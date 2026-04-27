package com.omra.platform.service;

import com.omra.platform.dto.*;
import com.omra.platform.entity.Agency;
import com.omra.platform.entity.TransportOffer;
import com.omra.platform.entity.TransportOfferReservation;
import com.omra.platform.entity.TransportVehicle;
import com.omra.platform.entity.enums.AgencyKind;
import com.omra.platform.entity.enums.TransportOfferStatus;
import com.omra.platform.entity.enums.HotelReservationStatus;
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

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransportOperatorService {

    private final AgencyRepository agencyRepository;
    private final TransportVehicleRepository vehicleRepository;
    private final TransportOfferRepository offerRepository;
    private final TransportOfferReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public TransportDashboardDto getDashboard() {
        Long agencyId = requireTransportAgencyId();
        long vehiclesCount = vehicleRepository.countByAgencyIdAndDeletedAtIsNull(agencyId);
        long offersCount = offerRepository.countByAgencyIdAndDeletedAtIsNull(agencyId);
        long activeOffersCount = offerRepository.countByAgencyIdAndStatusAndDeletedAtIsNull(agencyId, TransportOfferStatus.ACTIVE);
        long reservationsPending = reservationRepository.countByTransportAgencyIdAndStatus(agencyId, HotelReservationStatus.PENDING);
        long reservationsConfirmed = reservationRepository.countByTransportAgencyIdAndStatus(agencyId, HotelReservationStatus.CONFIRMED);
        long reservationsRejected = reservationRepository.countByTransportAgencyIdAndStatus(agencyId, HotelReservationStatus.REJECTED);
        long reservationsTotal = reservationRepository.countByTransportAgencyId(agencyId);
        List<TransportOfferReservation> recent = reservationRepository.findTop5ByTransportAgencyIdOrderByCreatedAtDesc(agencyId);
        Map<Long, String> travelAgencyNames = new HashMap<>();
        for (TransportOfferReservation r : recent) {
            Long tid = r.getTravelAgencyId();
            if (tid != null && !travelAgencyNames.containsKey(tid)) {
                agencyRepository.findById(tid).ifPresent(a -> travelAgencyNames.put(tid, a.getName()));
            }
        }
        List<TransportDashboardRecentReservationDto> recentDtos = recent.stream()
                .map(r -> TransportDashboardRecentReservationDto.builder()
                        .id(r.getId())
                        .contactName(r.getContactName())
                        .status(r.getStatus() != null ? r.getStatus().name() : null)
                        .createdAt(r.getCreatedAt())
                        .units(r.getUnits())
                        .travelAgencyName(travelAgencyNames.get(r.getTravelAgencyId()))
                        .build())
                .collect(Collectors.toList());
        return TransportDashboardDto.builder()
                .vehiclesCount(vehiclesCount)
                .offersCount(offersCount)
                .activeOffersCount(activeOffersCount)
                .reservationsPending(reservationsPending)
                .reservationsConfirmed(reservationsConfirmed)
                .reservationsRejected(reservationsRejected)
                .reservationsTotal(reservationsTotal)
                .recentReservations(recentDtos)
                .build();
    }

    @Transactional(readOnly = true)
    public List<TransportVehicleDto> listVehicles() {
        Long agencyId = requireTransportAgencyId();
        return vehicleRepository.findByAgencyIdAndDeletedAtIsNullOrderByPlateAsc(agencyId).stream()
                .map(this::toVehicleDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransportVehicleDto createVehicle(TransportVehicleWriteDto dto) {
        Long agencyId = requireTransportAgencyId();
        validateVehicleWrite(dto);
        TransportVehicle v = TransportVehicle.builder()
                .agencyId(agencyId)
                .vehicleType(dto.getVehicleType())
                .seatCount(dto.getSeatCount())
                .plate(blankToNull(dto.getPlate()))
                .brand(blankToNull(dto.getBrand()))
                .driverName(blankToNull(dto.getDriverName()))
                .driverPhone(blankToNull(dto.getDriverPhone()))
                .driverEmail(blankToNull(dto.getDriverEmail()))
                .address(blankToNull(dto.getAddress()))
                .build();
        return toVehicleDto(vehicleRepository.save(v));
    }

    @Transactional
    public TransportVehicleDto updateVehicle(Long id, TransportVehicleWriteDto dto) {
        Long agencyId = requireTransportAgencyId();
        TransportVehicle v = vehicleRepository.findByIdAndAgencyIdAndDeletedAtIsNull(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("TransportVehicle", id));
        validateVehicleWrite(dto);
        v.setVehicleType(dto.getVehicleType());
        v.setSeatCount(dto.getSeatCount());
        v.setPlate(blankToNull(dto.getPlate()));
        v.setBrand(blankToNull(dto.getBrand()));
        v.setDriverName(blankToNull(dto.getDriverName()));
        v.setDriverPhone(blankToNull(dto.getDriverPhone()));
        v.setDriverEmail(blankToNull(dto.getDriverEmail()));
        v.setAddress(blankToNull(dto.getAddress()));
        return toVehicleDto(vehicleRepository.save(v));
    }

    @Transactional
    public void deleteVehicle(Long id) {
        Long agencyId = requireTransportAgencyId();
        TransportVehicle v = vehicleRepository.findByIdAndAgencyIdAndDeletedAtIsNull(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("TransportVehicle", id));
        Instant now = Instant.now();
        for (TransportOffer o : offerRepository.findByAgencyIdAndVehicleIdAndDeletedAtIsNullOrderByCreatedAtDesc(agencyId, id)) {
            o.setDeletedAt(now);
            offerRepository.save(o);
        }
        v.setDeletedAt(now);
        vehicleRepository.save(v);
    }

    @Transactional(readOnly = true)
    public List<TransportOfferDto> listOffers(Long vehicleId) {
        Long agencyId = requireTransportAgencyId();
        List<TransportOffer> list = vehicleId != null
                ? offerRepository.findByAgencyIdAndVehicleIdAndDeletedAtIsNullOrderByCreatedAtDesc(agencyId, vehicleId)
                : offerRepository.findByAgencyIdAndDeletedAtIsNullOrderByCreatedAtDesc(agencyId);
        Map<Long, TransportVehicle> vehicles = loadVehicles(list);
        return list.stream().map(o -> toOfferDto(o, vehicles.get(o.getVehicleId()))).collect(Collectors.toList());
    }

    @Transactional
    public TransportOfferDto createOffer(TransportOfferWriteDto dto) {
        Long agencyId = requireTransportAgencyId();
        validateOfferWrite(dto);
        assertVehicleInAgency(dto.getVehicleId(), agencyId);
        Agency agency = agencyRepository.findById(agencyId).orElseThrow();
        String currency = dto.getCurrency() != null && !dto.getCurrency().isBlank()
                ? dto.getCurrency().trim()
                : defaultCurrency(agency);
        TransportOffer o = TransportOffer.builder()
                .agencyId(agencyId)
                .vehicleId(dto.getVehicleId())
                .title(dto.getTitle().trim())
                .description(blankToNull(dto.getDescription()))
                .imageUrl(blankToNull(dto.getImageUrl()))
                .pricingUnit(dto.getPricingUnit())
                .price(dto.getPrice())
                .currency(currency)
                .minUnits(dto.getMinUnits())
                .maxUnits(dto.getMaxUnits())
                .validFrom(dto.getValidFrom())
                .validTo(dto.getValidTo())
                .build();
        TransportOffer saved = offerRepository.save(o);
        TransportVehicle v = vehicleRepository.findByIdAndAgencyIdAndDeletedAtIsNull(saved.getVehicleId(), agencyId).orElseThrow();
        return toOfferDto(saved, v);
    }

    @Transactional
    public TransportOfferDto updateOffer(Long id, TransportOfferWriteDto dto) {
        Long agencyId = requireTransportAgencyId();
        TransportOffer o = offerRepository.findByIdAndAgencyIdAndDeletedAtIsNull(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("TransportOffer", id));
        validateOfferWrite(dto);
        if (!o.getVehicleId().equals(dto.getVehicleId())) {
            assertVehicleInAgency(dto.getVehicleId(), agencyId);
            o.setVehicleId(dto.getVehicleId());
        }
        o.setTitle(dto.getTitle().trim());
        o.setDescription(blankToNull(dto.getDescription()));
        o.setImageUrl(blankToNull(dto.getImageUrl()));
        o.setPricingUnit(dto.getPricingUnit());
        o.setPrice(dto.getPrice());
        if (dto.getCurrency() != null && !dto.getCurrency().isBlank()) {
            o.setCurrency(dto.getCurrency().trim());
        }
        o.setMinUnits(dto.getMinUnits());
        o.setMaxUnits(dto.getMaxUnits());
        o.setValidFrom(dto.getValidFrom());
        o.setValidTo(dto.getValidTo());
        TransportOffer saved = offerRepository.save(o);
        TransportVehicle v = vehicleRepository.findByIdAndAgencyIdAndDeletedAtIsNull(saved.getVehicleId(), agencyId).orElseThrow();
        return toOfferDto(saved, v);
    }

    @Transactional
    public TransportOfferDto setOfferStatus(Long offerId, boolean active) {
        Long agencyId = requireTransportAgencyId();
        TransportOffer o = offerRepository.findByIdAndAgencyIdAndDeletedAtIsNull(offerId, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("TransportOffer", offerId));
        o.setStatus(active ? TransportOfferStatus.ACTIVE : TransportOfferStatus.DISABLED);
        TransportOffer saved = offerRepository.save(o);
        TransportVehicle v = vehicleRepository.findByIdAndAgencyIdAndDeletedAtIsNull(saved.getVehicleId(), agencyId).orElseThrow();
        return toOfferDto(saved, v);
    }

    @Transactional
    public void deleteOffer(Long id) {
        Long agencyId = requireTransportAgencyId();
        TransportOffer o = offerRepository.findByIdAndAgencyIdAndDeletedAtIsNull(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("TransportOffer", id));
        o.setDeletedAt(Instant.now());
        offerRepository.save(o);
    }

    private Map<Long, TransportVehicle> loadVehicles(List<TransportOffer> offers) {
        Map<Long, TransportVehicle> map = new HashMap<>();
        for (TransportOffer o : offers) {
            if (o.getVehicleId() != null && !map.containsKey(o.getVehicleId())) {
                vehicleRepository.findById(o.getVehicleId()).ifPresent(v -> map.put(v.getId(), v));
            }
        }
        return map;
    }

    private void assertVehicleInAgency(Long vehicleId, Long agencyId) {
        vehicleRepository.findByIdAndAgencyIdAndDeletedAtIsNull(vehicleId, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("TransportVehicle", vehicleId));
    }

    private Long requireTransportAgencyId() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null) {
            throw new ForbiddenException("Agence requise.");
        }
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));
        if (agency.getAgencyKind() != AgencyKind.TRANSPORT) {
            throw new ForbiddenException("Réservé aux agences de type transport.");
        }
        return agencyId;
    }

    private void validateVehicleWrite(TransportVehicleWriteDto dto) {
        if (dto.getVehicleType() == null) {
            throw new BadRequestException("vehicleType est requis.");
        }
        if (dto.getSeatCount() == null || dto.getSeatCount() < 1) {
            throw new BadRequestException("seatCount doit être au moins 1.");
        }
    }

    private void validateOfferWrite(TransportOfferWriteDto dto) {
        if (dto.getVehicleId() == null) {
            throw new BadRequestException("vehicleId est requis.");
        }
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new BadRequestException("title est requis.");
        }
        if (dto.getPricingUnit() == null) {
            throw new BadRequestException("pricingUnit est requis.");
        }
        if (dto.getPrice() == null) {
            throw new BadRequestException("price est requis.");
        }
        if (dto.getValidFrom() == null || dto.getValidTo() == null) {
            throw new BadRequestException("validFrom et validTo sont requis.");
        }
        if (dto.getValidTo().isBefore(dto.getValidFrom())) {
            throw new BadRequestException("validTo doit être après validFrom.");
        }
    }

    private String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }

    private String defaultCurrency(Agency agency) {
        String c = agency.getCurrency();
        return c != null && !c.isBlank() ? c.trim() : "MAD";
    }

    private TransportVehicleDto toVehicleDto(TransportVehicle v) {
        return TransportVehicleDto.builder()
                .id(v.getId())
                .vehicleType(v.getVehicleType())
                .seatCount(v.getSeatCount())
                .plate(v.getPlate())
                .brand(v.getBrand())
                .driverName(v.getDriverName())
                .driverPhone(v.getDriverPhone())
                .driverEmail(v.getDriverEmail())
                .address(v.getAddress())
                .createdAt(v.getCreatedAt())
                .build();
    }

    private TransportOfferDto toOfferDto(TransportOffer o, TransportVehicle v) {
        return TransportOfferDto.builder()
                .id(o.getId())
                .vehicleId(o.getVehicleId())
                .transportAgencyId(o.getAgencyId())
                .transportAgencyName(null)
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
