package com.omra.platform.service;

import com.omra.platform.dto.AgencyDto;
import com.omra.platform.dto.AgencyMetricsDto;
import com.omra.platform.dto.AgencyThemeDto;
import com.omra.platform.entity.Agency;
import com.omra.platform.entity.User;
import com.omra.platform.entity.enums.AgencyStatus;
import com.omra.platform.entity.enums.PaymentStatus;
import com.omra.platform.entity.enums.UserRole;
import com.omra.platform.entity.enums.UserStatus;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.mapper.AgencyMapper;
import com.omra.platform.theme.AgencyThemeDefaults;
import com.omra.platform.theme.HexColorValidator;
import com.omra.platform.repository.AgencyRepository;
import com.omra.platform.repository.PaymentRepository;
import com.omra.platform.repository.PilgrimRepository;
import com.omra.platform.repository.UmrahGroupRepository;
import com.omra.platform.repository.UserRepository;
import com.omra.platform.dto.PageResponse;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgencyService {

    private final AgencyRepository agencyRepository;
    private final UserRepository userRepository;
    private final PilgrimRepository pilgrimRepository;
    private final UmrahGroupRepository umrahGroupRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AgencyMapper agencyMapper;

    @Transactional(readOnly = true)
    public PageResponse<AgencyDto> getAgencies(Pageable pageable) {
        if (!TenantContext.isSuperAdmin()) {
            throw new ForbiddenException("Only SUPER_ADMIN can list all agencies");
        }
        Page<Agency> page = agencyRepository.findAll(pageable);
        List<AgencyDto> content = page.getContent().stream().map(agencyMapper::toDto).collect(Collectors.toList());
        return PageResponse.<AgencyDto>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Transactional
    public AgencyDto create(AgencyDto dto) {
        if (!TenantContext.isSuperAdmin()) {
            throw new ForbiddenException("Only SUPER_ADMIN can create agencies");
        }
        Agency agency = Agency.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .country(dto.getCountry())
                .city(dto.getCity())
                .address(dto.getAddress())
                .logoUrl(dto.getLogoUrl())
                .faviconUrl(dto.getFaviconUrl())
                .subscriptionPlan(dto.getSubscriptionPlan())
                .subscriptionStartDate(dto.getSubscriptionStartDate())
                .subscriptionEndDate(dto.getSubscriptionEndDate())
                .status(dto.getStatus() != null ? dto.getStatus() : AgencyStatus.ACTIVE)
                .build();
        AgencyThemeDefaults.applyThemeOnCreate(agency, dto);
        agency = agencyRepository.save(agency);

        // Default agency admin user: email = agency email, name = "admin", password = "000000"
        if (!userRepository.existsByEmail(agency.getEmail())) {
            User defaultAdmin = User.builder()
                    .agencyId(agency.getId())
                    .name("admin")
                    .email(agency.getEmail())
                    .password(passwordEncoder.encode("000000"))
                    .role(UserRole.AGENCY_ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(defaultAdmin);
        }

        return agencyMapper.toDto(agency);
    }

    @Transactional
    public AgencyDto update(Long id, AgencyDto dto) {
        Agency agency = getAgencyForUpdate(id);
        if (dto.getName() != null) agency.setName(dto.getName());
        if (dto.getEmail() != null) agency.setEmail(dto.getEmail());
        if (dto.getPhone() != null) agency.setPhone(dto.getPhone());
        if (dto.getCountry() != null) agency.setCountry(dto.getCountry());
        if (dto.getCity() != null) agency.setCity(dto.getCity());
        if (dto.getAddress() != null) agency.setAddress(dto.getAddress());
        if (dto.getSubscriptionPlan() != null) agency.setSubscriptionPlan(dto.getSubscriptionPlan());
        if (dto.getSubscriptionStartDate() != null) agency.setSubscriptionStartDate(dto.getSubscriptionStartDate());
        if (dto.getSubscriptionEndDate() != null) agency.setSubscriptionEndDate(dto.getSubscriptionEndDate());
        if (dto.getStatus() != null) agency.setStatus(dto.getStatus());
        if (dto.getLogoUrl() != null) agency.setLogoUrl(dto.getLogoUrl());
        if (dto.getFaviconUrl() != null) agency.setFaviconUrl(dto.getFaviconUrl());
        patchHexColor(agency::setPrimaryColor, dto.getPrimaryColor(), "primaryColor");
        patchHexColor(agency::setSecondaryColor, dto.getSecondaryColor(), "secondaryColor");
        patchHexColor(agency::setSidebarColor, dto.getSidebarColor(), "sidebarColor");
        patchHexColor(agency::setMenuColor, dto.getMenuColor(), "menuColor");
        patchHexColor(agency::setButtonColor, dto.getButtonColor(), "buttonColor");
        patchHexColor(agency::setBackgroundColor, dto.getBackgroundColor(), "backgroundColor");
        patchHexColor(agency::setCardColor, dto.getCardColor(), "cardColor");
        patchHexColor(agency::setTextColor, dto.getTextColor(), "textColor");
        if (dto.getThemeMode() != null) agency.setThemeMode(dto.getThemeMode());
        AgencyThemeDefaults.fillMissingThemeFields(agency);
        agency = agencyRepository.save(agency);
        return agencyMapper.toDto(agency);
    }

    @Transactional(readOnly = true)
    public AgencyDto getById(Long id) {
        Agency agency = getAgencyForRead(id);
        return agencyMapper.toDto(agency);
    }

    @Transactional(readOnly = true)
    public AgencyMetricsDto getMetrics(Long agencyId) {
        getAgencyForRead(agencyId);
        BigDecimal paid = paymentRepository.sumAmountByAgencyIdAndStatus(agencyId, PaymentStatus.PAID);
        return AgencyMetricsDto.builder()
                .userCount(userRepository.countByAgencyIdAndDeletedAtIsNull(agencyId))
                .pilgrimCount(pilgrimRepository.countByAgencyIdAndDeletedAtIsNull(agencyId))
                .groupCount(umrahGroupRepository.countByAgencyIdAndDeletedAtIsNull(agencyId))
                .revenuePaid(paid != null ? paid : BigDecimal.ZERO)
                .build();
    }

    @Transactional
    public AgencyThemeDto getTheme() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null) return null;
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));
        return themeDtoAfterEnsuringDefaults(agency);
    }

    @Transactional
    public AgencyThemeDto getThemeForAgency(Long id) {
        Agency agency = getAgencyForRead(id);
        return themeDtoAfterEnsuringDefaults(agency);
    }

    @Transactional
    public AgencyThemeDto updateBranding(AgencyThemeDto dto) {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null) throw new ForbiddenException("No agency context");
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));
        if (dto.getLogoUrl() != null) agency.setLogoUrl(dto.getLogoUrl());
        if (dto.getFaviconUrl() != null) agency.setFaviconUrl(dto.getFaviconUrl());
        patchHexColor(agency::setPrimaryColor, dto.getPrimaryColor(), "primaryColor");
        patchHexColor(agency::setSecondaryColor, dto.getSecondaryColor(), "secondaryColor");
        patchHexColor(agency::setSidebarColor, dto.getSidebarColor(), "sidebarColor");
        patchHexColor(agency::setMenuColor, dto.getMenuColor(), "menuColor");
        patchHexColor(agency::setButtonColor, dto.getButtonColor(), "buttonColor");
        patchHexColor(agency::setBackgroundColor, dto.getBackgroundColor(), "backgroundColor");
        patchHexColor(agency::setCardColor, dto.getCardColor(), "cardColor");
        patchHexColor(agency::setTextColor, dto.getTextColor(), "textColor");
        if (dto.getThemeMode() != null) agency.setThemeMode(dto.getThemeMode());
        AgencyThemeDefaults.fillMissingThemeFields(agency);
        agencyRepository.save(agency);
        return agencyMapper.toThemeDto(agency);
    }

    /** Persiste les défauts si l’agence avait des champs vides (données historiques). */
    private AgencyThemeDto themeDtoAfterEnsuringDefaults(Agency agency) {
        boolean changed = AgencyThemeDefaults.fillMissingThemeFields(agency);
        if (changed) {
            agencyRepository.save(agency);
        }
        return agencyMapper.toThemeDto(agency);
    }

    private void patchHexColor(Consumer<String> setter, String value, String field) {
        if (value == null) {
            return;
        }
        if (value.isBlank()) {
            throw new BadRequestException(field + " cannot be empty; omit the field to keep the current value");
        }
        setter.accept(HexColorValidator.normalizeOrThrow(value, field));
    }

    private Agency getAgencyForRead(Long id) {
        if (TenantContext.isSuperAdmin()) {
            return agencyRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Agency", id));
        }
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null || !agencyId.equals(id)) {
            throw new ForbiddenException("Access denied to this agency");
        }
        return agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", id));
    }

    private Agency getAgencyForUpdate(Long id) {
        return getAgencyForRead(id);
    }
}
