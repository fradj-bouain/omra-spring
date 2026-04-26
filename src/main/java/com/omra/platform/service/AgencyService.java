package com.omra.platform.service;

import com.omra.platform.dto.AgencyDto;
import com.omra.platform.dto.AgencyMetricsDto;
import com.omra.platform.dto.AgencyThemeDto;
import com.omra.platform.dto.SubAgencyQuotaDto;
import com.omra.platform.entity.Agency;
import com.omra.platform.entity.AgencySubscription;
import com.omra.platform.entity.SubscriptionPlan;
import com.omra.platform.entity.User;
import com.omra.platform.entity.enums.AgencyKind;
import com.omra.platform.entity.enums.AgencyStatus;
import com.omra.platform.entity.enums.AgencySubscriptionStatus;
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
import com.omra.platform.repository.AgencySubscriptionRepository;
import com.omra.platform.repository.PaymentRepository;
import com.omra.platform.repository.PilgrimRepository;
import com.omra.platform.repository.SubscriptionPlanRepository;
import com.omra.platform.repository.UmrahGroupRepository;
import com.omra.platform.repository.UserRepository;
import com.omra.platform.dto.PageResponse;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final AgencySubscriptionRepository agencySubscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
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
                .currency(dto.getCurrency())
                .city(dto.getCity())
                .address(dto.getAddress())
                .logoUrl(dto.getLogoUrl())
                .faviconUrl(dto.getFaviconUrl())
                .subscriptionPlan(dto.getSubscriptionPlan())
                .subscriptionStartDate(dto.getSubscriptionStartDate())
                .subscriptionEndDate(dto.getSubscriptionEndDate())
                .status(dto.getStatus() != null ? dto.getStatus() : AgencyStatus.ACTIVE)
                .agencyKind(dto.getAgencyKind() != null ? dto.getAgencyKind() : AgencyKind.TRAVEL)
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

    /**
     * Creates a sub-agency under a main (root) agency. {@link AgencyDto#getEmail()} must be unique.
     */
    @Transactional
    public AgencyDto createSubAgency(Long parentId, AgencyDto dto) {
        Agency parent = agencyRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", parentId));
        if (parent.getParentAgencyId() != null) {
            throw new BadRequestException("Les sous-agences ne peuvent pas avoir de sous-agences.");
        }
        if (!TenantContext.isSuperAdmin()) {
            if (TenantContext.getUserRole() != UserRole.AGENCY_ADMIN) {
                throw new ForbiddenException("Seul un administrateur d'agence peut créer une sous-agence.");
            }
            if (!parentId.equals(TenantContext.getAgencyId())) {
                throw new ForbiddenException("Vous ne pouvez créer une sous-agence que pour votre agence principale.");
            }
        }
        assertSubAgencyQuotaAllowsNew(parentId);
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        Agency agency = Agency.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .country(dto.getCountry())
                .currency(dto.getCurrency())
                .city(dto.getCity())
                .address(dto.getAddress())
                .logoUrl(dto.getLogoUrl())
                .faviconUrl(dto.getFaviconUrl())
                .subscriptionPlan(dto.getSubscriptionPlan())
                .subscriptionStartDate(dto.getSubscriptionStartDate())
                .subscriptionEndDate(dto.getSubscriptionEndDate())
                .status(dto.getStatus() != null ? dto.getStatus() : AgencyStatus.ACTIVE)
                .parentAgencyId(parentId)
                .agencyKind(parent.getAgencyKind() != null ? parent.getAgencyKind() : AgencyKind.TRAVEL)
                .build();
        AgencyThemeDefaults.applyThemeOnCreate(agency, dto);
        agency = agencyRepository.save(agency);

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

    @Transactional(readOnly = true)
    public List<AgencyDto> listSubAgencies(Long parentId) {
        Agency parent = agencyRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", parentId));
        if (parent.getParentAgencyId() != null) {
            throw new BadRequestException("Le parent doit être une agence principale.");
        }
        if (!TenantContext.canAccessAgencyId(parentId)) {
            throw new ForbiddenException("Access denied to this agency");
        }
        return agencyRepository.findByParentAgencyId(parentId).stream()
                .map(agencyMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubAgencyQuotaDto getSubAgencyQuota(Long parentId) {
        Agency parent = agencyRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", parentId));
        if (parent.getParentAgencyId() != null) {
            throw new BadRequestException("Le quota des sous-agences s'applique uniquement à une agence principale.");
        }
        if (!TenantContext.canAccessAgencyId(parentId)) {
            throw new ForbiddenException("Access denied to this agency");
        }
        int active = (int) agencyRepository.countByParentAgencyIdAndStatus(parentId, AgencyStatus.ACTIVE);
        Integer max = resolveMaxSubAgenciesFromPlan(parentId);
        boolean canCreate = max == null || active < max;
        return SubAgencyQuotaDto.builder()
                .activeSubAgencies(active)
                .maxSubAgencies(max)
                .canCreate(canCreate)
                .build();
    }

    @Transactional
    public AgencyDto deactivateSubAgency(Long parentId, Long subAgencyId) {
        Agency sub = agencyRepository.findById(subAgencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", subAgencyId));
        if (sub.getParentAgencyId() == null) {
            throw new BadRequestException("Seules les sous-agences peuvent être désactivées depuis cet écran.");
        }
        if (!sub.getParentAgencyId().equals(parentId)) {
            throw new BadRequestException("Cette sous-agence n'appartient pas à cette agence principale.");
        }
        if (!TenantContext.isSuperAdmin()) {
            if (TenantContext.getUserRole() != UserRole.AGENCY_ADMIN) {
                throw new ForbiddenException("Seul un administrateur d'agence peut désactiver une sous-agence.");
            }
            if (!parentId.equals(TenantContext.getAgencyId())) {
                throw new ForbiddenException("Vous ne pouvez gérer que les sous-agences de votre agence principale.");
            }
        }
        sub.setStatus(AgencyStatus.SUSPENDED);
        return agencyMapper.toDto(agencyRepository.save(sub));
    }

    @Transactional
    public AgencyDto update(Long id, AgencyDto dto) {
        Agency agency = getAgencyForUpdate(id);
        if (dto.getName() != null) agency.setName(dto.getName());
        if (dto.getEmail() != null) agency.setEmail(dto.getEmail());
        if (dto.getPhone() != null) agency.setPhone(dto.getPhone());
        if (dto.getCountry() != null) agency.setCountry(dto.getCountry());
        if (dto.getCurrency() != null) agency.setCurrency(dto.getCurrency());
        if (dto.getCity() != null) agency.setCity(dto.getCity());
        if (dto.getAddress() != null) agency.setAddress(dto.getAddress());
        if (dto.getSubscriptionPlan() != null) agency.setSubscriptionPlan(dto.getSubscriptionPlan());
        if (dto.getSubscriptionStartDate() != null) agency.setSubscriptionStartDate(dto.getSubscriptionStartDate());
        if (dto.getSubscriptionEndDate() != null) agency.setSubscriptionEndDate(dto.getSubscriptionEndDate());
        if (dto.getStatus() != null) agency.setStatus(dto.getStatus());
        if (dto.getAgencyKind() != null) {
            if (!TenantContext.isSuperAdmin()) {
                throw new ForbiddenException("Seul le super-administrateur peut modifier le type d'agence.");
            }
            agency.setAgencyKind(dto.getAgencyKind());
        }
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
        if (!TenantContext.canAccessAgencyId(id)) {
            throw new ForbiddenException("Access denied to this agency");
        }
        return agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", id));
    }

    private Agency getAgencyForUpdate(Long id) {
        return getAgencyForRead(id);
    }

    /**
     * {@code null} = unlimited; {@code 0} = no valid subscription or plan allows no subs.
     */
    private Integer resolveMaxSubAgenciesFromPlan(Long mainAgencyId) {
        List<AgencySubscription> valid = agencySubscriptionRepository.findValidPaidCovering(
                mainAgencyId, AgencySubscriptionStatus.ACTIVE, LocalDate.now(), PageRequest.of(0, 1));
        if (valid.isEmpty()) {
            return 0;
        }
        SubscriptionPlan plan = subscriptionPlanRepository.findById(valid.get(0).getPlanId()).orElse(null);
        if (plan == null) {
            return 0;
        }
        return plan.getMaxSubAgencies();
    }

    private void assertSubAgencyQuotaAllowsNew(Long mainAgencyId) {
        Integer max = resolveMaxSubAgenciesFromPlan(mainAgencyId);
        long active = agencyRepository.countByParentAgencyIdAndStatus(mainAgencyId, AgencyStatus.ACTIVE);
        if (max != null && active >= max) {
            throw new BadRequestException(
                    "Le nombre de sous-agences actives autorisé par votre abonnement est atteint. "
                            + "Désactivez une sous-agence ou souscrivez à un forfait avec un quota plus élevé.");
        }
    }
}
