package com.omra.platform.service;

import com.omra.platform.dto.AgencySubscriptionDto;
import com.omra.platform.dto.AgencySubscriptionSummaryDto;
import com.omra.platform.dto.AssignAgencySubscriptionRequest;
import com.omra.platform.dto.PageResponse;
import com.omra.platform.dto.UpdateAgencySubscriptionRequest;
import com.omra.platform.entity.Agency;
import com.omra.platform.entity.AgencySubscription;
import com.omra.platform.entity.SubscriptionPlan;
import com.omra.platform.entity.enums.AgencyStatus;
import com.omra.platform.entity.enums.AgencySubscriptionStatus;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.AgencyRepository;
import com.omra.platform.repository.AgencySubscriptionRepository;
import com.omra.platform.repository.SubscriptionPlanRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgencySubscriptionService {

    private final AgencySubscriptionRepository agencySubscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final AgencyRepository agencyRepository;

    private void requireSuperAdmin() {
        if (!TenantContext.isSuperAdmin()) {
            throw new ForbiddenException("Réservé au super-administrateur");
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<AgencySubscriptionDto> listByAgency(Long agencyId, int page, int size) {
        requireSuperAdmin();
        agencyRepository.findById(agencyId).orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));
        int p = Math.max(0, page);
        int s = Math.max(1, size);
        Page<AgencySubscription> pg = agencySubscriptionRepository.findByAgencyIdOrderByPeriodEndDesc(agencyId, PageRequest.of(p, s));
        return PageResponse.<AgencySubscriptionDto>builder()
                .content(pg.getContent().stream().map(this::toDto).collect(Collectors.toList()))
                .page(pg.getNumber())
                .size(pg.getSize())
                .totalElements(pg.getTotalElements())
                .totalPages(pg.getTotalPages())
                .first(pg.isFirst())
                .last(pg.isLast())
                .build();
    }

    /** Dernier enregistrement créé (pour affichage admin). */
    @Transactional(readOnly = true)
    public AgencySubscriptionDto getLatest(Long agencyId) {
        requireSuperAdmin();
        agencyRepository.findById(agencyId).orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));
        return agencySubscriptionRepository.findTopByAgencyIdOrderByIdDesc(agencyId).map(this::toDto).orElse(null);
    }

    /** Abonnement payé couvrant aujourd'hui, s'il existe. */
    @Transactional(readOnly = true)
    public AgencySubscriptionDto getCurrentValid(Long agencyId) {
        requireSuperAdmin();
        List<AgencySubscription> list = agencySubscriptionRepository.findValidPaidCovering(
                agencyId, AgencySubscriptionStatus.ACTIVE, LocalDate.now(), PageRequest.of(0, 1));
        return list.isEmpty() ? null : toDto(list.get(0));
    }

    /** Résumé abonnement pour l’agence du JWT (portail agence, sans notes internes). */
    @Transactional(readOnly = true)
    public AgencySubscriptionSummaryDto summaryForMyAgency() {
        Long agencyId = requireAgencyUserContext();
        agencyRepository.findById(agencyId).orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));
        AgencySubscriptionDto latest = agencySubscriptionRepository.findTopByAgencyIdOrderByIdDesc(agencyId)
                .map(this::toSelfServiceDto)
                .orElse(null);
        List<AgencySubscription> list = agencySubscriptionRepository.findValidPaidCovering(
                agencyId, AgencySubscriptionStatus.ACTIVE, LocalDate.now(), PageRequest.of(0, 1));
        AgencySubscriptionDto currentValid = list.isEmpty() ? null : toSelfServiceDto(list.get(0));
        return AgencySubscriptionSummaryDto.builder()
                .latest(latest)
                .currentValid(currentValid)
                .build();
    }

    /** Historique paginé pour l’agence du JWT (portail agence, sans notes internes). */
    @Transactional(readOnly = true)
    public PageResponse<AgencySubscriptionDto> listForMyAgency(int page, int size) {
        Long agencyId = requireAgencyUserContext();
        agencyRepository.findById(agencyId).orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));
        int p = Math.max(0, page);
        int s = Math.max(1, Math.min(50, size));
        Page<AgencySubscription> pg = agencySubscriptionRepository.findByAgencyIdOrderByPeriodEndDesc(agencyId, PageRequest.of(p, s));
        return PageResponse.<AgencySubscriptionDto>builder()
                .content(pg.getContent().stream().map(this::toSelfServiceDto).collect(Collectors.toList()))
                .page(pg.getNumber())
                .size(pg.getSize())
                .totalElements(pg.getTotalElements())
                .totalPages(pg.getTotalPages())
                .first(pg.isFirst())
                .last(pg.isLast())
                .build();
    }

    private Long requireAgencyUserContext() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null) {
            throw new ForbiddenException("Abonnement : compte non rattaché à une agence");
        }
        return agencyId;
    }

    /** Masque les notes internes (réservées au super-admin). */
    private AgencySubscriptionDto toSelfServiceDto(AgencySubscription s) {
        AgencySubscriptionDto dto = toDto(s);
        dto.setNotes(null);
        return dto;
    }

    @Transactional
    public AgencySubscriptionDto assign(Long agencyId, AssignAgencySubscriptionRequest req) {
        requireSuperAdmin();
        Agency agency = agencyRepository.findById(agencyId).orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));
        SubscriptionPlan plan = subscriptionPlanRepository.findById(req.getPlanId()).orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", req.getPlanId()));
        if (!Boolean.TRUE.equals(plan.getActive())) {
            throw new BadRequestException("Ce forfait est désactivé");
        }
        validatePeriod(req.getPeriodStart(), req.getPeriodEnd());

        cancelActiveSubscriptions(agencyId);

        boolean paid = req.isMarkAsPaid();
        AgencySubscription sub = AgencySubscription.builder()
                .agencyId(agencyId)
                .planId(plan.getId())
                .periodStart(req.getPeriodStart())
                .periodEnd(req.getPeriodEnd())
                .status(paid ? AgencySubscriptionStatus.ACTIVE : AgencySubscriptionStatus.PENDING_PAYMENT)
                .paidAt(paid ? Instant.now() : null)
                .paymentReference(req.getPaymentReference())
                .amountPaid(req.getAmountPaid() != null ? req.getAmountPaid() : (paid ? plan.getPrice() : null))
                .currency(StringUtils.hasText(req.getCurrency()) ? req.getCurrency() : plan.getCurrency())
                .notes(req.getNotes())
                .build();
        sub = agencySubscriptionRepository.save(sub);
        applyAgencyStateAfterSubscriptionChange(agency, sub, plan);
        return toDto(sub);
    }

    @Transactional
    public AgencySubscriptionDto update(Long agencyId, Long subscriptionId, UpdateAgencySubscriptionRequest req) {
        requireSuperAdmin();
        Agency agency = agencyRepository.findById(agencyId).orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));
        AgencySubscription sub = agencySubscriptionRepository.findById(subscriptionId).orElseThrow(() -> new ResourceNotFoundException("AgencySubscription", subscriptionId));
        if (!sub.getAgencyId().equals(agencyId)) {
            throw new BadRequestException("Cet abonnement n'appartient pas à cette agence");
        }
        if (req.getPeriodStart() != null) {
            sub.setPeriodStart(req.getPeriodStart());
        }
        if (req.getPeriodEnd() != null) {
            sub.setPeriodEnd(req.getPeriodEnd());
        }
        validatePeriod(sub.getPeriodStart(), sub.getPeriodEnd());
        if (req.getStatus() != null) {
            sub.setStatus(req.getStatus());
        }
        if (Boolean.TRUE.equals(req.getMarkAsPaid()) && sub.getPaidAt() == null) {
            sub.setPaidAt(Instant.now());
            if (sub.getStatus() == AgencySubscriptionStatus.PENDING_PAYMENT) {
                sub.setStatus(AgencySubscriptionStatus.ACTIVE);
            }
        }
        if (Boolean.FALSE.equals(req.getMarkAsPaid())) {
            sub.setPaidAt(null);
            if (sub.getStatus() == AgencySubscriptionStatus.ACTIVE) {
                sub.setStatus(AgencySubscriptionStatus.PENDING_PAYMENT);
            }
        }
        if (req.getPaymentReference() != null) {
            sub.setPaymentReference(req.getPaymentReference());
        }
        if (req.getAmountPaid() != null) {
            sub.setAmountPaid(req.getAmountPaid());
        }
        if (StringUtils.hasText(req.getCurrency())) {
            sub.setCurrency(req.getCurrency());
        }
        if (req.getNotes() != null) {
            sub.setNotes(req.getNotes());
        }
        sub = agencySubscriptionRepository.save(sub);
        if (sub.getStatus() == AgencySubscriptionStatus.ACTIVE && sub.getPaidAt() != null) {
            cancelOtherActiveAndPendingExcept(agencyId, sub.getId());
        }
        refreshAgencyAccessState(agencyId);
        return toDto(sub);
    }

    /** Garde un seul abonnement actif payé « principal » : annule les autres actifs / en attente. */
    private void cancelOtherActiveAndPendingExcept(Long agencyId, Long keepSubscriptionId) {
        for (AgencySubscription o : agencySubscriptionRepository.findByAgencyIdAndStatus(agencyId, AgencySubscriptionStatus.ACTIVE)) {
            if (!o.getId().equals(keepSubscriptionId)) {
                o.setStatus(AgencySubscriptionStatus.CANCELLED);
                agencySubscriptionRepository.save(o);
            }
        }
        for (AgencySubscription o : agencySubscriptionRepository.findByAgencyIdAndStatus(agencyId, AgencySubscriptionStatus.PENDING_PAYMENT)) {
            o.setStatus(AgencySubscriptionStatus.CANCELLED);
            agencySubscriptionRepository.save(o);
        }
    }

    /** Recalcule statut agence + champs dérivés à partir des abonnements. */
    @Transactional
    public void refreshAgencyAccessState(Long agencyId) {
        requireSuperAdmin();
        Agency agency = agencyRepository.findById(agencyId).orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));
        AgencySubscription latest = agencySubscriptionRepository.findTopByAgencyIdOrderByIdDesc(agencyId).orElse(null);
        if (latest == null) {
            agency.setSubscriptionPlan(null);
            agency.setSubscriptionStartDate(null);
            agency.setSubscriptionEndDate(null);
            agency.setStatus(AgencyStatus.SUSPENDED);
            agencyRepository.save(agency);
            return;
        }
        SubscriptionPlan plan = subscriptionPlanRepository.findById(latest.getPlanId()).orElse(null);
        if (plan != null) {
            agency.setSubscriptionPlan(plan.getName());
            agency.setSubscriptionStartDate(latest.getPeriodStart());
            agency.setSubscriptionEndDate(latest.getPeriodEnd());
        }
        boolean ok = !agencySubscriptionRepository
                .findValidPaidCovering(agencyId, AgencySubscriptionStatus.ACTIVE, LocalDate.now(), PageRequest.of(0, 1))
                .isEmpty();
        agency.setStatus(ok ? AgencyStatus.ACTIVE : AgencyStatus.SUSPENDED);
        agencyRepository.save(agency);
    }

    private void cancelActiveSubscriptions(Long agencyId) {
        List<AgencySubscription> actives = agencySubscriptionRepository.findByAgencyIdAndStatus(agencyId, AgencySubscriptionStatus.ACTIVE);
        for (AgencySubscription o : actives) {
            o.setStatus(AgencySubscriptionStatus.CANCELLED);
            agencySubscriptionRepository.save(o);
        }
        List<AgencySubscription> pending = agencySubscriptionRepository.findByAgencyIdAndStatus(agencyId, AgencySubscriptionStatus.PENDING_PAYMENT);
        for (AgencySubscription o : pending) {
            o.setStatus(AgencySubscriptionStatus.CANCELLED);
            agencySubscriptionRepository.save(o);
        }
    }

    private void applyAgencyStateAfterSubscriptionChange(Agency agency, AgencySubscription sub, SubscriptionPlan plan) {
        agency.setSubscriptionPlan(plan.getName());
        agency.setSubscriptionStartDate(sub.getPeriodStart());
        agency.setSubscriptionEndDate(sub.getPeriodEnd());
        boolean ok = sub.getStatus() == AgencySubscriptionStatus.ACTIVE
                && sub.getPaidAt() != null
                && !LocalDate.now().isBefore(sub.getPeriodStart())
                && !LocalDate.now().isAfter(sub.getPeriodEnd());
        agency.setStatus(ok ? AgencyStatus.ACTIVE : AgencyStatus.SUSPENDED);
        agencyRepository.save(agency);
    }

    private static void validatePeriod(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new BadRequestException("Les dates de période sont obligatoires");
        }
        if (end.isBefore(start)) {
            throw new BadRequestException("La date de fin doit être postérieure ou égale au début");
        }
    }

    private AgencySubscriptionDto toDto(AgencySubscription s) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(s.getPlanId()).orElse(null);
        return AgencySubscriptionDto.builder()
                .id(s.getId())
                .agencyId(s.getAgencyId())
                .planId(s.getPlanId())
                .planCode(plan != null ? plan.getCode() : null)
                .planName(plan != null ? plan.getName() : null)
                .periodStart(s.getPeriodStart())
                .periodEnd(s.getPeriodEnd())
                .status(s.getStatus())
                .paidAt(s.getPaidAt())
                .paymentReference(s.getPaymentReference())
                .amountPaid(s.getAmountPaid())
                .currency(s.getCurrency())
                .notes(s.getNotes())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
