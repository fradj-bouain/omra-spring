package com.omra.platform.service;

import com.omra.platform.dto.CreatePilgrimFamilyBatchRequestDto;
import com.omra.platform.dto.CreatePilgrimFamilyBatchResponseDto;
import com.omra.platform.dto.FamilyMemberPayloadDto;
import com.omra.platform.dto.PageResponse;
import com.omra.platform.dto.PilgrimDto;
import com.omra.platform.dto.PilgrimSearchResultDto;
import com.omra.platform.entity.Pilgrim;
import com.omra.platform.entity.PilgrimFamily;
import com.omra.platform.entity.enums.SponsorType;
import com.omra.platform.entity.enums.TravelerType;
import com.omra.platform.entity.enums.VisaStatus;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.PilgrimFamilyRepository;
import com.omra.platform.repository.PilgrimRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PilgrimService {

    private final PilgrimRepository pilgrimRepository;
    private final PilgrimFamilyRepository pilgrimFamilyRepository;
    private final NotificationProducerService notificationProducer;
    private final PilgrimSponsorshipService pilgrimSponsorshipService;

    private Long requireAgencyId() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null && !TenantContext.isSuperAdmin()) {
            throw new ForbiddenException("Agency context required");
        }
        return agencyId;
    }

    @Transactional(readOnly = true)
    public PageResponse<PilgrimDto> getPilgrims(Pageable pageable) {
        Long agencyId = requireAgencyId();
        if (TenantContext.isSuperAdmin() && agencyId == null) {
            Page<Pilgrim> page = pilgrimRepository.findByDeletedAtIsNull(pageable);
            return toPageResponse(page);
        }
        List<Long> scoped = Objects.requireNonNullElse(TenantContext.getScopedAgencyIdsForQueries(), List.of());
        if (scoped.isEmpty()) {
            throw new ForbiddenException("Agency context required");
        }
        Page<Pilgrim> page = scoped.size() == 1
                ? pilgrimRepository.findByAgencyIdAndDeletedAtIsNull(scoped.get(0), pageable)
                : pilgrimRepository.findByAgencyIdInAndDeletedAtIsNull(scoped, pageable);
        return toPageResponse(page);
    }

    @Transactional(readOnly = true)
    public PilgrimDto getById(Long id) {
        Pilgrim pilgrim = findByIdAndAgency(id);
        return toDtoWithEnrichment(pilgrim);
    }

    /** Autocomplete pour choisir un pèlerin parrain (min. 2 caractères). */
    @Transactional(readOnly = true)
    public List<PilgrimSearchResultDto> autocompletePilgrims(String q, int limit) {
        Long agencyId = requireAgencyId();
        if (agencyId == null) {
            return Collections.emptyList();
        }
        if (q == null || q.trim().length() < 2) {
            return Collections.emptyList();
        }
        int lim = Math.min(Math.max(limit, 1), 50);
        Pageable pageable = PageRequest.of(0, lim);
        List<Long> scoped = Objects.requireNonNullElse(TenantContext.getScopedAgencyIdsForQueries(), List.of());
        if (scoped.isEmpty()) {
            return Collections.emptyList();
        }
        List<Pilgrim> found = scoped.size() == 1
                ? pilgrimRepository.searchForAutocomplete(scoped.get(0), q.trim(), pageable)
                : pilgrimRepository.searchForAutocomplete(scoped, q.trim(), pageable);
        return found.stream()
                .map(p -> PilgrimSearchResultDto.builder()
                        .id(p.getId())
                        .firstName(p.getFirstName())
                        .lastName(p.getLastName())
                        .passportNumber(p.getPassportNumber())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public PilgrimDto create(PilgrimDto dto) {
        Long agencyId = requireAgencyId();
        if (agencyId == null) throw new ForbiddenException("Agency required to create pilgrim");
        if (dto.getPassportNumber() != null && !dto.getPassportNumber().isBlank()
                && pilgrimRepository.existsByAgencyIdAndPassportNumberAndDeletedAtIsNull(agencyId, dto.getPassportNumber().trim())) {
            throw new BadRequestException("Un voyageur avec ce numéro de passeport existe déjà pour cette agence.");
        }
        if (dto.getSponsorType() == SponsorType.PILGRIM && dto.getReferrerPilgrimId() == null) {
            throw new BadRequestException("Pour un parrain de type voyageur, sélectionnez le voyageur parrain dans la liste.");
        }
        Pilgrim pilgrim = Pilgrim.builder()
                .agencyId(agencyId)
                .familyId(null)
                .familyRole(null)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .gender(dto.getGender())
                .dateOfBirth(dto.getDateOfBirth())
                .passportNumber(dto.getPassportNumber())
                .passportIssueDate(dto.getPassportIssueDate())
                .passportExpiry(dto.getPassportExpiry())
                .nationality(dto.getNationality())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .address(dto.getAddress())
                .photoUrl(dto.getPhotoUrl())
                .passportScanUrl(dto.getPassportScanUrl())
                .visaStatus(dto.getVisaStatus() != null ? dto.getVisaStatus() : VisaStatus.PENDING)
                .travelerType(dto.getTravelerType() != null ? dto.getTravelerType() : TravelerType.PILGRIM)
                .sponsorType(dto.getSponsorType())
                .sponsorLabel(trimOrNull(dto.getSponsorLabel()))
                .referrerPilgrimId(dto.getSponsorType() == SponsorType.PILGRIM ? dto.getReferrerPilgrimId() : null)
                .referralPoints(0)
                .build();
        pilgrim = pilgrimRepository.save(pilgrim);
        pilgrimSponsorshipService.afterPilgrimCreated(pilgrim, dto);
        return toDtoWithEnrichment(pilgrim);
    }

    /**
     * Crée une entrée {@code pilgrim_families} et plusieurs pèlerins liés (même {@code family_id}),
     * en une transaction. Utilisé par le flux « famille » du front.
     */
    @Transactional
    public CreatePilgrimFamilyBatchResponseDto createFamilyBatch(CreatePilgrimFamilyBatchRequestDto req) {
        Long agencyId = requireAgencyId();
        if (agencyId == null) {
            throw new ForbiddenException("Agency required to create pilgrims");
        }
        List<FamilyMemberPayloadDto> members = req.getMembers();
        if (members == null || members.size() < 2) {
            throw new BadRequestException("Au moins 2 membres sont requis pour une famille.");
        }
        if (req.getSponsorType() == SponsorType.PILGRIM && req.getReferrerPilgrimId() == null) {
            throw new BadRequestException("Pour un parrain de type voyageur, sélectionnez le voyageur parrain dans la liste.");
        }

        Set<String> seenPassports = new HashSet<>();
        for (FamilyMemberPayloadDto m : members) {
            String raw = m.getPassportNumber();
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String p = raw.trim();
            if (!seenPassports.add(p)) {
                throw new BadRequestException("Numéro de pièce d’identité dupliqué dans la liste des membres.");
            }
            if (pilgrimRepository.existsByAgencyIdAndPassportNumberAndDeletedAtIsNull(agencyId, p)) {
                throw new BadRequestException("Un voyageur avec ce numéro de passeport existe déjà pour cette agence.");
            }
        }

        TravelerType travelerType = req.getTravelerType() != null ? req.getTravelerType() : TravelerType.PILGRIM;

        PilgrimFamily family = pilgrimFamilyRepository.save(PilgrimFamily.builder()
                .agencyId(agencyId)
                .build());

        VisaStatus visa = req.getVisaStatus() != null ? req.getVisaStatus() : VisaStatus.PENDING;
        List<PilgrimDto> createdDtos = new ArrayList<>();

        for (FamilyMemberPayloadDto m : members) {
            String mergedAddress = mergeDocumentNotesToAddress(req.getAddress(), m.getDocumentNotes());
            Pilgrim pilgrim = Pilgrim.builder()
                    .agencyId(agencyId)
                    .familyId(family.getId())
                    .familyRole(trimOrNull(m.getFamilyRole()))
                    .firstName(m.getFirstName().trim())
                    .lastName(m.getLastName().trim())
                    .gender(trimOrNull(m.getGender()))
                    .dateOfBirth(m.getDateOfBirth())
                    .passportNumber(trimOrNull(m.getPassportNumber()))
                    .nationality(trimOrNull(req.getNationality()))
                    .phone(trimOrNull(req.getPhone()))
                    .email(trimOrNull(req.getEmail()))
                    .address(mergedAddress)
                    .visaStatus(visa)
                    .travelerType(travelerType)
                    .sponsorType(req.getSponsorType())
                    .sponsorLabel(trimOrNull(req.getSponsorLabel()))
                    .referrerPilgrimId(req.getSponsorType() == SponsorType.PILGRIM ? req.getReferrerPilgrimId() : null)
                    .referralPoints(0)
                    .build();
            pilgrim = pilgrimRepository.save(pilgrim);

            PilgrimDto forSponsor = PilgrimDto.builder()
                    .sponsorType(req.getSponsorType())
                    .referrerPilgrimId(req.getSponsorType() == SponsorType.PILGRIM ? req.getReferrerPilgrimId() : null)
                    .build();
            pilgrimSponsorshipService.afterPilgrimCreated(pilgrim, forSponsor);

            createdDtos.add(toDtoWithEnrichment(pilgrim));
        }

        return CreatePilgrimFamilyBatchResponseDto.builder()
                .familyId(family.getId())
                .pilgrims(createdDtos)
                .build();
    }

    private static String mergeDocumentNotesToAddress(String baseAddress, String documentNotes) {
        String notes = trimOrNull(documentNotes);
        if (notes == null) {
            return trimOrNull(baseAddress);
        }
        String line = "Réf. documents : " + notes;
        String base = trimOrNull(baseAddress);
        if (base == null) {
            return line;
        }
        return base + "\n" + line;
    }

    @Transactional
    public PilgrimDto update(Long id, PilgrimDto dto) {
        Pilgrim pilgrim = findByIdAndAgency(id);
        if (dto.getPassportNumber() != null && !dto.getPassportNumber().isBlank()
                && pilgrimRepository.existsByAgencyIdAndPassportNumberAndDeletedAtIsNullAndIdNot(pilgrim.getAgencyId(), dto.getPassportNumber().trim(), id)) {
            throw new BadRequestException("Un voyageur avec ce numéro de passeport existe déjà pour cette agence.");
        }
        if (dto.getFirstName() != null) pilgrim.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) pilgrim.setLastName(dto.getLastName());
        if (dto.getGender() != null) pilgrim.setGender(dto.getGender());
        if (dto.getDateOfBirth() != null) pilgrim.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getPassportNumber() != null) pilgrim.setPassportNumber(dto.getPassportNumber());
        if (dto.getPassportIssueDate() != null) pilgrim.setPassportIssueDate(dto.getPassportIssueDate());
        if (dto.getPassportExpiry() != null) pilgrim.setPassportExpiry(dto.getPassportExpiry());
        if (dto.getNationality() != null) pilgrim.setNationality(dto.getNationality());
        if (dto.getPhone() != null) pilgrim.setPhone(dto.getPhone());
        if (dto.getEmail() != null) pilgrim.setEmail(dto.getEmail());
        if (dto.getAddress() != null) pilgrim.setAddress(dto.getAddress());
        if (dto.getPhotoUrl() != null) pilgrim.setPhotoUrl(dto.getPhotoUrl());
        if (dto.getPassportScanUrl() != null) pilgrim.setPassportScanUrl(dto.getPassportScanUrl());
        VisaStatus oldVisa = pilgrim.getVisaStatus();
        if (dto.getVisaStatus() != null) pilgrim.setVisaStatus(dto.getVisaStatus());
        if (dto.getTravelerType() != null) pilgrim.setTravelerType(dto.getTravelerType());
        pilgrim = pilgrimRepository.save(pilgrim);
        if (dto.getVisaStatus() != null && dto.getVisaStatus() != oldVisa) {
            String name = pilgrim.getFirstName() + " " + pilgrim.getLastName();
            notificationProducer.notifyVisaStatusChange(pilgrim.getAgencyId(), pilgrim.getId(), name, pilgrim.getVisaStatus().name());
        }
        return toDtoWithEnrichment(pilgrim);
    }

    @Transactional
    public void delete(Long id) {
        Pilgrim pilgrim = findByIdAndAgency(id);
        pilgrim.setDeletedAt(Instant.now());
        pilgrimRepository.save(pilgrim);
    }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private Pilgrim findByIdAndAgency(Long id) {
        Pilgrim pilgrim = pilgrimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pilgrim", id));
        if (!TenantContext.isSuperAdmin() && !TenantContext.canAccessAgencyId(pilgrim.getAgencyId())) {
            throw new ForbiddenException("Access denied to this pilgrim");
        }
        if (pilgrim.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Pilgrim", id);
        }
        return pilgrim;
    }

    private PageResponse<PilgrimDto> toPageResponse(Page<Pilgrim> page) {
        List<PilgrimDto> content = page.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return PageResponse.<PilgrimDto>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    private PilgrimDto toDto(Pilgrim e) {
        return PilgrimDto.builder()
                .id(e.getId())
                .agencyId(e.getAgencyId())
                .familyId(e.getFamilyId())
                .familyRole(e.getFamilyRole())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .gender(e.getGender())
                .dateOfBirth(e.getDateOfBirth())
                .passportNumber(e.getPassportNumber())
                .passportIssueDate(e.getPassportIssueDate())
                .passportExpiry(e.getPassportExpiry())
                .nationality(e.getNationality())
                .phone(e.getPhone())
                .email(e.getEmail())
                .address(e.getAddress())
                .photoUrl(e.getPhotoUrl())
                .passportScanUrl(e.getPassportScanUrl())
                .visaStatus(e.getVisaStatus())
                .travelerType(e.getTravelerType())
                .createdAt(e.getCreatedAt())
                .sponsorType(e.getSponsorType())
                .sponsorLabel(e.getSponsorLabel())
                .referrerPilgrimId(e.getReferrerPilgrimId())
                .referralPoints(e.getReferralPoints())
                .build();
    }

    private PilgrimDto toDtoWithEnrichment(Pilgrim e) {
        PilgrimDto d = toDto(e);
        pilgrimSponsorshipService.enrichPilgrimDto(d, e, e.getAgencyId());
        return d;
    }
}
