package com.omra.platform.service;

import com.omra.platform.dto.PageResponse;
import com.omra.platform.dto.PilgrimDto;
import com.omra.platform.entity.Pilgrim;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.entity.enums.VisaStatus;
import com.omra.platform.repository.PilgrimRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PilgrimService {

    private final PilgrimRepository pilgrimRepository;
    private final NotificationProducerService notificationProducer;

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
        Page<Pilgrim> page = pilgrimRepository.findByAgencyIdAndDeletedAtIsNull(agencyId, pageable);
        return toPageResponse(page);
    }

    @Transactional(readOnly = true)
    public PilgrimDto getById(Long id) {
        Pilgrim pilgrim = findByIdAndAgency(id);
        return toDto(pilgrim);
    }

    @Transactional
    public PilgrimDto create(PilgrimDto dto) {
        Long agencyId = requireAgencyId();
        if (agencyId == null) throw new ForbiddenException("Agency required to create pilgrim");
        if (dto.getPassportNumber() != null && !dto.getPassportNumber().isBlank()
                && pilgrimRepository.existsByAgencyIdAndPassportNumberAndDeletedAtIsNull(agencyId, dto.getPassportNumber().trim())) {
            throw new BadRequestException("Un pèlerin avec ce numéro de passeport existe déjà pour cette agence.");
        }
        Pilgrim pilgrim = Pilgrim.builder()
                .agencyId(agencyId)
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
                .visaStatus(dto.getVisaStatus() != null ? dto.getVisaStatus() : com.omra.platform.entity.enums.VisaStatus.PENDING)
                .build();
        pilgrim = pilgrimRepository.save(pilgrim);
        return toDto(pilgrim);
    }

    @Transactional
    public PilgrimDto update(Long id, PilgrimDto dto) {
        Pilgrim pilgrim = findByIdAndAgency(id);
        if (dto.getPassportNumber() != null && !dto.getPassportNumber().isBlank()
                && pilgrimRepository.existsByAgencyIdAndPassportNumberAndDeletedAtIsNullAndIdNot(pilgrim.getAgencyId(), dto.getPassportNumber().trim(), id)) {
            throw new BadRequestException("Un pèlerin avec ce numéro de passeport existe déjà pour cette agence.");
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
        pilgrim = pilgrimRepository.save(pilgrim);
        if (dto.getVisaStatus() != null && dto.getVisaStatus() != oldVisa) {
            String name = pilgrim.getFirstName() + " " + pilgrim.getLastName();
            notificationProducer.notifyVisaStatusChange(pilgrim.getAgencyId(), pilgrim.getId(), name, pilgrim.getVisaStatus().name());
        }
        return toDto(pilgrim);
    }

    @Transactional
    public void delete(Long id) {
        Pilgrim pilgrim = findByIdAndAgency(id);
        pilgrim.setDeletedAt(Instant.now());
        pilgrimRepository.save(pilgrim);
    }

    private Pilgrim findByIdAndAgency(Long id) {
        Pilgrim pilgrim = pilgrimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pilgrim", id));
        Long agencyId = TenantContext.getAgencyId();
        if (!TenantContext.isSuperAdmin() && (agencyId == null || !agencyId.equals(pilgrim.getAgencyId()))) {
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
                .createdAt(e.getCreatedAt())
                .build();
    }
}
