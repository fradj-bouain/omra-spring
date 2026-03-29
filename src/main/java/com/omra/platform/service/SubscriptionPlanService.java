package com.omra.platform.service;

import com.omra.platform.dto.SubscriptionPlanDto;
import com.omra.platform.entity.SubscriptionPlan;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.SubscriptionPlanRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    private void requireSuperAdmin() {
        if (!TenantContext.isSuperAdmin()) {
            throw new ForbiddenException("Réservé au super-administrateur");
        }
    }

    @Transactional(readOnly = true)
    public List<SubscriptionPlanDto> listAll() {
        requireSuperAdmin();
        return subscriptionPlanRepository.findAllByOrderBySortOrderAscNameAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SubscriptionPlanDto> listActive() {
        requireSuperAdmin();
        return subscriptionPlanRepository.findByActiveTrueOrderBySortOrderAscNameAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubscriptionPlanDto getById(Long id) {
        requireSuperAdmin();
        return toDto(subscriptionPlanRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", id)));
    }

    @Transactional
    public SubscriptionPlanDto create(SubscriptionPlanDto dto) {
        requireSuperAdmin();
        String code = normalizeCode(dto.getCode());
        if (!StringUtils.hasText(code)) {
            throw new BadRequestException("Le code du forfait est obligatoire");
        }
        if (subscriptionPlanRepository.findByCode(code).isPresent()) {
            throw new BadRequestException("Ce code de forfait existe déjà");
        }
        SubscriptionPlan e = SubscriptionPlan.builder()
                .code(code)
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice() != null ? dto.getPrice() : java.math.BigDecimal.ZERO)
                .currency(StringUtils.hasText(dto.getCurrency()) ? dto.getCurrency() : "MAD")
                .billingPeriod(dto.getBillingPeriod())
                .defaultDurationDays(dto.getDefaultDurationDays())
                .maxUsers(dto.getMaxUsers())
                .features(dto.getFeatures())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .build();
        if (e.getBillingPeriod() == null) {
            throw new BadRequestException("La période de facturation est obligatoire");
        }
        if (!StringUtils.hasText(e.getName())) {
            throw new BadRequestException("Le nom est obligatoire");
        }
        return toDto(subscriptionPlanRepository.save(e));
    }

    @Transactional
    public SubscriptionPlanDto update(Long id, SubscriptionPlanDto dto) {
        requireSuperAdmin();
        SubscriptionPlan e = subscriptionPlanRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", id));
        if ("LEGACY".equalsIgnoreCase(e.getCode()) && dto.getActive() != null && !dto.getActive()) {
            throw new BadRequestException("Le forfait technique LEGACY ne peut pas être désactivé");
        }
        if (StringUtils.hasText(dto.getName())) {
            e.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            e.setDescription(dto.getDescription());
        }
        if (dto.getPrice() != null) {
            e.setPrice(dto.getPrice());
        }
        if (StringUtils.hasText(dto.getCurrency())) {
            e.setCurrency(dto.getCurrency());
        }
        if (dto.getBillingPeriod() != null) {
            e.setBillingPeriod(dto.getBillingPeriod());
        }
        if (dto.getDefaultDurationDays() != null) {
            e.setDefaultDurationDays(dto.getDefaultDurationDays());
        }
        if (dto.getMaxUsers() != null) {
            e.setMaxUsers(dto.getMaxUsers());
        }
        if (dto.getFeatures() != null) {
            e.setFeatures(dto.getFeatures());
        }
        if (dto.getActive() != null) {
            e.setActive(dto.getActive());
        }
        if (dto.getSortOrder() != null) {
            e.setSortOrder(dto.getSortOrder());
        }
        return toDto(subscriptionPlanRepository.save(e));
    }

    private static String normalizeCode(String code) {
        if (code == null) return null;
        return code.trim().toUpperCase().replaceAll("\\s+", "_");
    }

    private SubscriptionPlanDto toDto(SubscriptionPlan e) {
        return SubscriptionPlanDto.builder()
                .id(e.getId())
                .code(e.getCode())
                .name(e.getName())
                .description(e.getDescription())
                .price(e.getPrice())
                .currency(e.getCurrency())
                .billingPeriod(e.getBillingPeriod())
                .defaultDurationDays(e.getDefaultDurationDays())
                .maxUsers(e.getMaxUsers())
                .features(e.getFeatures())
                .active(e.getActive())
                .sortOrder(e.getSortOrder())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
