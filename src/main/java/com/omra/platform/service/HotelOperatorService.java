package com.omra.platform.service;

import com.omra.platform.dto.*;
import com.omra.platform.entity.Agency;
import com.omra.platform.entity.HotelOffer;
import com.omra.platform.entity.HotelProperty;
import com.omra.platform.entity.enums.AgencyKind;
import com.omra.platform.entity.enums.HotelOfferStatus;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.AgencyRepository;
import com.omra.platform.repository.HotelOfferRepository;
import com.omra.platform.repository.HotelPropertyRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelOperatorService {

    private final AgencyRepository agencyRepository;
    private final HotelPropertyRepository propertyRepository;
    private final HotelOfferRepository offerRepository;

    @Transactional(readOnly = true)
    public List<HotelPropertyDto> listProperties() {
        Long agencyId = requireHotelAgencyId();
        return propertyRepository.findByAgencyIdOrderByNameAsc(agencyId).stream()
                .map(this::toPropertyDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public HotelPropertyDto createProperty(HotelPropertyWriteDto dto) {
        Long agencyId = requireHotelAgencyId();
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("name est requis.");
        }
        HotelProperty p = HotelProperty.builder()
                .agencyId(agencyId)
                .name(dto.getName().trim())
                .description(blankToNull(dto.getDescription()))
                .city(blankToNull(dto.getCity()))
                .country(blankToNull(dto.getCountry()))
                .address(blankToNull(dto.getAddress()))
                .imageUrl(blankToNull(dto.getImageUrl()))
                .build();
        return toPropertyDto(propertyRepository.save(p));
    }

    @Transactional
    public HotelPropertyDto updateProperty(Long id, HotelPropertyWriteDto dto) {
        Long agencyId = requireHotelAgencyId();
        HotelProperty p = propertyRepository.findByIdAndAgencyId(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("HotelProperty", id));
        if (dto.getName() != null && !dto.getName().isBlank()) {
            p.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            p.setDescription(blankToNull(dto.getDescription()));
        }
        if (dto.getCity() != null) {
            p.setCity(blankToNull(dto.getCity()));
        }
        if (dto.getCountry() != null) {
            p.setCountry(blankToNull(dto.getCountry()));
        }
        if (dto.getAddress() != null) {
            p.setAddress(blankToNull(dto.getAddress()));
        }
        if (dto.getImageUrl() != null) {
            p.setImageUrl(blankToNull(dto.getImageUrl()));
        }
        return toPropertyDto(propertyRepository.save(p));
    }

    @Transactional
    public void deleteProperty(Long id) {
        Long agencyId = requireHotelAgencyId();
        HotelProperty p = propertyRepository.findByIdAndAgencyId(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("HotelProperty", id));
        propertyRepository.delete(p);
    }

    @Transactional(readOnly = true)
    public List<HotelOfferDto> listOffers(Long propertyId) {
        Long agencyId = requireHotelAgencyId();
        if (propertyId != null) {
            assertPropertyInAgency(propertyId, agencyId);
            return offerRepository.findByAgencyIdAndPropertyIdOrderByCreatedAtDesc(agencyId, propertyId).stream()
                    .map(this::toOfferDto)
                    .collect(Collectors.toList());
        }
        return offerRepository.findByAgencyIdOrderByCreatedAtDesc(agencyId).stream()
                .map(this::toOfferDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public HotelOfferDto createOffer(HotelOfferWriteDto dto) {
        Long agencyId = requireHotelAgencyId();
        validateOfferWrite(dto);
        assertPropertyInAgency(dto.getPropertyId(), agencyId);
        Agency agency = agencyRepository.findById(agencyId).orElseThrow();
        String currency = dto.getCurrency() != null && !dto.getCurrency().isBlank()
                ? dto.getCurrency().trim()
                : defaultCurrency(agency);
        HotelOffer o = HotelOffer.builder()
                .agencyId(agencyId)
                .propertyId(dto.getPropertyId())
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
        return toOfferDto(offerRepository.save(o));
    }

    @Transactional
    public HotelOfferDto updateOffer(Long id, HotelOfferWriteDto dto) {
        Long agencyId = requireHotelAgencyId();
        HotelOffer o = offerRepository.findByIdAndAgencyId(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("HotelOffer", id));
        validateOfferWrite(dto);
        if (!o.getPropertyId().equals(dto.getPropertyId())) {
            assertPropertyInAgency(dto.getPropertyId(), agencyId);
            o.setPropertyId(dto.getPropertyId());
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
        return toOfferDto(offerRepository.save(o));
    }

    @Transactional
    public HotelOfferDto setOfferStatus(Long offerId, boolean active) {
        Long agencyId = requireHotelAgencyId();
        HotelOffer o = offerRepository.findByIdAndAgencyId(offerId, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("HotelOffer", offerId));
        o.setStatus(active ? HotelOfferStatus.ACTIVE : HotelOfferStatus.DISABLED);
        return toOfferDto(offerRepository.save(o));
    }

    @Transactional
    public void deleteOffer(Long id) {
        Long agencyId = requireHotelAgencyId();
        HotelOffer o = offerRepository.findByIdAndAgencyId(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("HotelOffer", id));
        offerRepository.delete(o);
    }

    private void assertPropertyInAgency(Long propertyId, Long agencyId) {
        propertyRepository.findByIdAndAgencyId(propertyId, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("HotelProperty", propertyId));
    }

    private Long requireHotelAgencyId() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null) {
            throw new ForbiddenException("Agence requise.");
        }
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));
        if (agency.getAgencyKind() != AgencyKind.HOTEL) {
            throw new ForbiddenException("Réservé aux agences de type hôtel.");
        }
        return agencyId;
    }

    private void validateOfferWrite(HotelOfferWriteDto dto) {
        if (dto.getPropertyId() == null) {
            throw new BadRequestException("propertyId est requis.");
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

    private HotelPropertyDto toPropertyDto(HotelProperty p) {
        return HotelPropertyDto.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .city(p.getCity())
                .country(p.getCountry())
                .address(p.getAddress())
                .imageUrl(p.getImageUrl())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private HotelOfferDto toOfferDto(HotelOffer o) {
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
