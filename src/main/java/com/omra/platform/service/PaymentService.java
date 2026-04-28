package com.omra.platform.service;

import com.omra.platform.dto.PageResponse;
import com.omra.platform.dto.PaymentDto;
import com.omra.platform.dto.PaymentDueDto;
import com.omra.platform.entity.Payment;
import com.omra.platform.entity.PaymentDue;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.entity.Pilgrim;
import com.omra.platform.entity.UmrahGroup;
import com.omra.platform.entity.enums.PaymentDueStatus;
import com.omra.platform.entity.enums.PaymentStatus;
import com.omra.platform.repository.PaymentDueRepository;
import com.omra.platform.repository.PaymentRepository;
import com.omra.platform.repository.PilgrimRepository;
import com.omra.platform.repository.UmrahGroupRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentDueRepository paymentDueRepository;
    private final PilgrimRepository pilgrimRepository;
    private final UmrahGroupRepository umrahGroupRepository;
    private final NotificationProducerService notificationProducer;

    private Long requireAgencyId() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null && !TenantContext.isSuperAdmin()) throw new ForbiddenException("Agency context required");
        return agencyId;
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentDto> getPayments(Pageable pageable, Long agencyFilter) {
        Page<Payment> page;
        if (TenantContext.isSuperAdmin()) {
            if (agencyFilter != null) {
                page = paymentRepository.findByAgencyIdAndDeletedAtIsNull(agencyFilter, pageable);
            } else {
                page = paymentRepository.findByDeletedAtIsNull(pageable);
            }
        } else {
            requireAgencyId();
            List<Long> scoped = Objects.requireNonNullElse(TenantContext.getScopedAgencyIdsForQueries(), List.of());
            if (scoped.isEmpty()) {
                throw new ForbiddenException("Agency context required");
            }
            page = scoped.size() == 1
                    ? paymentRepository.findByAgencyIdAndDeletedAtIsNull(scoped.get(0), pageable)
                    : paymentRepository.findByAgencyIdInAndDeletedAtIsNull(scoped, pageable);
        }
        return toPageResponse(page);
    }

    @Transactional(readOnly = true)
    public PaymentDto getById(Long id) {
        Payment payment = findByIdAndAgency(id);
        Map<Long, String> pilgrimNames = batchPilgrimNames(List.of(payment));
        Map<Long, String> groupNames = batchGroupNames(List.of(payment));
        return toDto(payment, pilgrimNames, groupNames);
    }

    @Transactional
    public PaymentDto create(PaymentDto dto) {
        Long agencyId = requireAgencyId();
        if (agencyId == null) throw new ForbiddenException("Agency required");
        if (dto.getPilgrimId() == null) throw new BadRequestException("Le pèlerin est obligatoire.");
        if (dto.getGroupId() == null) throw new BadRequestException("Le groupe est obligatoire.");
        PaymentStatus status = dto.getStatus() != null ? dto.getStatus() : PaymentStatus.PENDING;
        boolean partialWithSchedule =
                status == PaymentStatus.PARTIAL && dto.getDueDates() != null && !dto.getDueDates().isEmpty();
        if (status == PaymentStatus.PARTIAL && !partialWithSchedule) {
            if (dto.getFirstDueDate() == null) {
                throw new BadRequestException("Pour un paiement partiel, la date de première échéance est obligatoire.");
            }
            if (dto.getNumberOfInstallments() == null || dto.getNumberOfInstallments() < 2) {
                throw new BadRequestException("Pour un paiement partiel, le nombre d'échéances doit être au moins 2.");
            }
            if (dto.getDuePeriodDays() == null || dto.getDuePeriodDays() < 1) {
                throw new BadRequestException("Pour un paiement partiel, la période entre échéances (en jours) est obligatoire.");
            }
        }
        Integer numInst =
                dto.getNumberOfInstallments() != null
                        ? dto.getNumberOfInstallments()
                        : (partialWithSchedule ? dto.getDueDates().size() : null);
        Payment payment =
                Payment.builder()
                        .agencyId(agencyId)
                        .pilgrimId(dto.getPilgrimId())
                        .groupId(dto.getGroupId())
                        .amount(dto.getAmount())
                        .currency(dto.getCurrency() != null ? dto.getCurrency() : "MAD")
                        .paymentMethod(dto.getPaymentMethod())
                        .status(status)
                        .paymentDate(dto.getPaymentDate())
                        .reference(dto.getReference())
                        .firstDueDate(dto.getFirstDueDate())
                        .duePeriodDays(dto.getDuePeriodDays())
                        .numberOfInstallments(numInst)
                        .build();
        payment = paymentRepository.save(payment);
        if (status == PaymentStatus.PARTIAL) {
            if (partialWithSchedule) {
                validateDueSchedule(dto.getAmount(), dto.getDueDates());
                upsertDueDates(payment.getId(), dto.getDueDates());
                syncPartialMetaFromDues(payment, dto.getDueDates(), dto.getDuePeriodDays());
                payment = paymentRepository.save(payment);
            } else if (dto.getFirstDueDate() != null
                    && dto.getNumberOfInstallments() != null
                    && dto.getDuePeriodDays() != null) {
                generateDueDates(payment);
            }
        }
        if (payment.getStatus() == PaymentStatus.PAID) {
            String pilgrimName = pilgrimName(payment.getPilgrimId());
            notificationProducer.notifyPaymentReceived(payment.getAgencyId(), payment.getId(), pilgrimName, payment.getAmount() + " " + payment.getCurrency());
        }
        return toDto(payment, batchPilgrimNames(List.of(payment)), batchGroupNames(List.of(payment)));
    }

    private void generateDueDates(Payment payment) {
        LocalDate date = payment.getFirstDueDate();
        int n = payment.getNumberOfInstallments();
        int periodDays = payment.getDuePeriodDays() != null ? payment.getDuePeriodDays() : 30;
        BigDecimal total = payment.getAmount();
        BigDecimal amountPerDue = total.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
        List<PaymentDue> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(PaymentDue.builder()
                    .paymentId(payment.getId())
                    .dueDate(date.plusDays((long) i * periodDays))
                    .amount(amountPerDue)
                    .status(PaymentDueStatus.PENDING)
                    .sequenceOrder(i + 1)
                    .build());
        }
        paymentDueRepository.saveAll(list);
    }

    private void validateDueSchedule(BigDecimal total, List<PaymentDueDto> dues) {
        if (total == null) {
            throw new BadRequestException("Montant requis.");
        }
        if (dues == null || dues.size() < 2) {
            throw new BadRequestException("Au moins 2 échéances sont requises pour un paiement partiel.");
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (PaymentDueDto d : dues) {
            if (d.getDueDate() == null) {
                throw new BadRequestException("Chaque échéance doit avoir une date.");
            }
            if (d.getAmount() == null || d.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Chaque échéance doit avoir un montant positif.");
            }
            sum = sum.add(d.getAmount());
        }
        if (sum.subtract(total).abs().compareTo(new BigDecimal("0.02")) > 0) {
            throw new BadRequestException("La somme des échéances doit égaler le montant total du paiement.");
        }
    }

    /**
     * Crée / met à jour les échéances sans effacer les statuts déjà payés si les lignes sont conservées (même id).
     * Les lignes absentes de la liste sont supprimées.
     */
    private void upsertDueDates(Long paymentId, List<PaymentDueDto> dues) {
        List<PaymentDueDto> sorted =
                dues.stream()
                        .sorted(
                                Comparator.comparing(
                                        d -> d.getSequenceOrder() != null ? d.getSequenceOrder() : Integer.MAX_VALUE))
                        .collect(Collectors.toList());

        Set<Long> incomingIds =
                sorted.stream().map(PaymentDueDto::getId).filter(Objects::nonNull).collect(Collectors.toSet());

        List<PaymentDue> existing = paymentDueRepository.findByPaymentIdOrderBySequenceOrderAsc(paymentId);
        for (PaymentDue e : existing) {
            if (!incomingIds.contains(e.getId())) {
                paymentDueRepository.delete(e);
            }
        }

        int fallbackSeq = 1;
        for (PaymentDueDto d : sorted) {
            PaymentDueStatus st = d.getStatus() != null ? d.getStatus() : PaymentDueStatus.PENDING;
            int order = d.getSequenceOrder() != null ? d.getSequenceOrder() : fallbackSeq;
            if (d.getId() != null) {
                PaymentDue ent =
                        paymentDueRepository
                                .findByIdAndPaymentId(d.getId(), paymentId)
                                .orElseThrow(() -> new BadRequestException("Échéance introuvable ou invalide."));
                ent.setDueDate(d.getDueDate());
                ent.setAmount(d.getAmount());
                ent.setSequenceOrder(order);
                ent.setStatus(st);
                paymentDueRepository.save(ent);
            } else {
                paymentDueRepository.save(
                        PaymentDue.builder()
                                .paymentId(paymentId)
                                .dueDate(d.getDueDate())
                                .amount(d.getAmount())
                                .status(st)
                                .sequenceOrder(order)
                                .build());
            }
            fallbackSeq++;
        }
    }

    @Transactional
    public PaymentDto markInstallmentPaid(Long paymentId, Long dueId) {
        Payment payment = findByIdAndAgency(paymentId);
        PaymentDue due =
                paymentDueRepository
                        .findByIdAndPaymentId(dueId, paymentId)
                        .orElseThrow(() -> new ResourceNotFoundException("PaymentDue", dueId));
        if (due.getStatus() != PaymentDueStatus.PAID) {
            due.setStatus(PaymentDueStatus.PAID);
            paymentDueRepository.save(due);
        }
        refreshParentPaymentIfAllInstallmentsPaid(payment);
        payment = paymentRepository.findById(paymentId).orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
        return toDto(payment, batchPilgrimNames(List.of(payment)), batchGroupNames(List.of(payment)));
    }

    private void refreshParentPaymentIfAllInstallmentsPaid(Payment payment) {
        if (payment.getStatus() != PaymentStatus.PARTIAL) {
            return;
        }
        List<PaymentDue> list = paymentDueRepository.findByPaymentIdOrderBySequenceOrderAsc(payment.getId());
        if (list.isEmpty()) {
            return;
        }
        boolean allPaid = list.stream().allMatch(d -> d.getStatus() == PaymentDueStatus.PAID);
        if (!allPaid) {
            return;
        }
        PaymentStatus before = payment.getStatus();
        payment.setStatus(PaymentStatus.PAID);
        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDate.now());
        }
        paymentRepository.save(payment);
        if (before != PaymentStatus.PAID) {
            String pn = pilgrimName(payment.getPilgrimId());
            notificationProducer.notifyPaymentReceived(
                    payment.getAgencyId(), payment.getId(), pn, payment.getAmount() + " " + payment.getCurrency());
        }
    }

    private void syncPartialMetaFromDues(Payment payment, List<PaymentDueDto> dues, Integer duePeriodDaysFromDto) {
        List<PaymentDueDto> byDate =
                dues.stream().sorted(Comparator.comparing(PaymentDueDto::getDueDate)).collect(Collectors.toList());
        payment.setFirstDueDate(byDate.get(0).getDueDate());
        payment.setNumberOfInstallments(dues.size());
        if (duePeriodDaysFromDto != null) {
            payment.setDuePeriodDays(duePeriodDaysFromDto);
        }
    }

    @Transactional
    public PaymentDto update(Long id, PaymentDto dto) {
        Payment payment = findByIdAndAgency(id);
        PaymentStatus before = payment.getStatus();
        if (dto.getAmount() != null) payment.setAmount(dto.getAmount());
        if (dto.getCurrency() != null) payment.setCurrency(dto.getCurrency());
        if (dto.getPaymentMethod() != null) payment.setPaymentMethod(dto.getPaymentMethod());
        if (dto.getStatus() != null) payment.setStatus(dto.getStatus());
        if (dto.getPaymentDate() != null) payment.setPaymentDate(dto.getPaymentDate());
        if (dto.getReference() != null) payment.setReference(dto.getReference());
        if (dto.getFirstDueDate() != null) payment.setFirstDueDate(dto.getFirstDueDate());
        if (dto.getDuePeriodDays() != null) payment.setDuePeriodDays(dto.getDuePeriodDays());
        if (dto.getNumberOfInstallments() != null) payment.setNumberOfInstallments(dto.getNumberOfInstallments());
        payment = paymentRepository.save(payment);

        PaymentStatus after = payment.getStatus();
        if (after == PaymentStatus.PARTIAL && dto.getDueDates() != null && !dto.getDueDates().isEmpty()) {
            validateDueSchedule(payment.getAmount(), dto.getDueDates());
            upsertDueDates(payment.getId(), dto.getDueDates());
            syncPartialMetaFromDues(payment, dto.getDueDates(), dto.getDuePeriodDays());
            payment = paymentRepository.save(payment);
        } else if (before == PaymentStatus.PARTIAL && after != PaymentStatus.PARTIAL) {
            paymentDueRepository.deleteByPaymentId(payment.getId());
        }

        if (payment.getStatus() == PaymentStatus.PAID && before != PaymentStatus.PAID) {
            String pilgrimName = pilgrimName(payment.getPilgrimId());
            notificationProducer.notifyPaymentReceived(payment.getAgencyId(), payment.getId(), pilgrimName, payment.getAmount() + " " + payment.getCurrency());
        }
        return toDto(payment, batchPilgrimNames(List.of(payment)), batchGroupNames(List.of(payment)));
    }

    @Transactional(readOnly = true)
    public List<PaymentDueDto> getDueDatesByPaymentId(Long paymentId) {
        findByIdAndAgency(paymentId);
        return paymentDueRepository.findByPaymentIdOrderBySequenceOrderAsc(paymentId).stream()
                .map(this::toDueDto)
                .collect(Collectors.toList());
    }

    private String pilgrimName(Long pilgrimId) {
        if (pilgrimId == null) return null;
        return pilgrimRepository.findById(pilgrimId)
                .map(p -> p.getFirstName() + " " + p.getLastName())
                .orElse(null);
    }

    @Transactional
    public void delete(Long id) {
        Payment payment = findByIdAndAgency(id);
        payment.setDeletedAt(Instant.now());
        paymentRepository.save(payment);
    }

    private Payment findByIdAndAgency(Long id) {
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payment", id));
        if (!TenantContext.isSuperAdmin() && !TenantContext.canAccessAgencyId(payment.getAgencyId())) {
            throw new ForbiddenException("Access denied");
        }
        if (payment.getDeletedAt() != null) throw new ResourceNotFoundException("Payment", id);
        return payment;
    }

    private PageResponse<PaymentDto> toPageResponse(Page<Payment> page) {
        List<Payment> payments = page.getContent();
        Map<Long, String> pilgrimNames = batchPilgrimNames(payments);
        Map<Long, String> groupNames = batchGroupNames(payments);
        List<PaymentDto> content = payments.stream()
                .map(p -> toDto(p, pilgrimNames, groupNames))
                .collect(Collectors.toList());
        return PageResponse.<PaymentDto>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    private Map<Long, String> batchPilgrimNames(List<Payment> payments) {
        List<Long> ids = payments.stream().map(Payment::getPilgrimId).filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> map = new HashMap<>();
        for (Pilgrim p : pilgrimRepository.findAllById(ids)) {
            map.put(p.getId(), formatPilgrimName(p));
        }
        return map;
    }

    private Map<Long, String> batchGroupNames(List<Payment> payments) {
        List<Long> ids = payments.stream().map(Payment::getGroupId).filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> map = new HashMap<>();
        for (UmrahGroup g : umrahGroupRepository.findAllById(ids)) {
            map.put(g.getId(), g.getName() != null ? g.getName() : "");
        }
        return map;
    }

    private static String formatPilgrimName(Pilgrim p) {
        String fn = p.getFirstName() != null ? p.getFirstName().trim() : "";
        String ln = p.getLastName() != null ? p.getLastName().trim() : "";
        String s = (fn + " " + ln).trim();
        return s.isEmpty() ? null : s;
    }

    private PaymentDto toDto(Payment e, Map<Long, String> pilgrimNames, Map<Long, String> groupNames) {
        List<PaymentDueDto> dueDtos = paymentDueRepository.findByPaymentIdOrderBySequenceOrderAsc(e.getId()).stream()
                .map(this::toDueDto)
                .collect(Collectors.toList());
        Long pid = e.getPilgrimId();
        Long gid = e.getGroupId();
        String pilgrimDisplay = pid != null ? pilgrimNames.get(pid) : null;
        String groupDisplay = gid != null ? groupNames.get(gid) : null;
        return PaymentDto.builder()
                .id(e.getId())
                .agencyId(e.getAgencyId())
                .pilgrimId(pid)
                .pilgrimName(pilgrimDisplay)
                .groupId(gid)
                .groupName(groupDisplay)
                .amount(e.getAmount())
                .currency(e.getCurrency())
                .paymentMethod(e.getPaymentMethod())
                .status(e.getStatus())
                .paymentDate(e.getPaymentDate())
                .reference(e.getReference())
                .firstDueDate(e.getFirstDueDate())
                .duePeriodDays(e.getDuePeriodDays())
                .numberOfInstallments(e.getNumberOfInstallments())
                .dueDates(dueDtos)
                .createdAt(e.getCreatedAt())
                .build();
    }

    private PaymentDueDto toDueDto(PaymentDue d) {
        return PaymentDueDto.builder()
                .id(d.getId())
                .paymentId(d.getPaymentId())
                .dueDate(d.getDueDate())
                .amount(d.getAmount())
                .status(d.getStatus())
                .sequenceOrder(d.getSequenceOrder())
                .build();
    }
}
