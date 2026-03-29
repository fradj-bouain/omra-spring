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
import com.omra.platform.entity.enums.PaymentDueStatus;
import com.omra.platform.entity.enums.PaymentStatus;
import com.omra.platform.repository.PaymentDueRepository;
import com.omra.platform.repository.PaymentRepository;
import com.omra.platform.repository.PilgrimRepository;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentDueRepository paymentDueRepository;
    private final PilgrimRepository pilgrimRepository;
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
            Long agencyId = requireAgencyId();
            if (agencyId == null) {
                throw new ForbiddenException("Agency context required");
            }
            page = paymentRepository.findByAgencyIdAndDeletedAtIsNull(agencyId, pageable);
        }
        return toPageResponse(page);
    }

    @Transactional(readOnly = true)
    public PaymentDto getById(Long id) {
        return toDto(findByIdAndAgency(id));
    }

    @Transactional
    public PaymentDto create(PaymentDto dto) {
        Long agencyId = requireAgencyId();
        if (agencyId == null) throw new ForbiddenException("Agency required");
        if (dto.getPilgrimId() == null) throw new BadRequestException("Le pèlerin est obligatoire.");
        if (dto.getGroupId() == null) throw new BadRequestException("Le groupe est obligatoire.");
        PaymentStatus status = dto.getStatus() != null ? dto.getStatus() : PaymentStatus.PENDING;
        if (status == PaymentStatus.PARTIAL) {
            if (dto.getFirstDueDate() == null) throw new BadRequestException("Pour un paiement partiel, la date de première échéance est obligatoire.");
            if (dto.getNumberOfInstallments() == null || dto.getNumberOfInstallments() < 2) throw new BadRequestException("Pour un paiement partiel, le nombre d'échéances doit être au moins 2.");
            if (dto.getDuePeriodDays() == null || dto.getDuePeriodDays() < 1) throw new BadRequestException("Pour un paiement partiel, la période entre échéances (en jours) est obligatoire.");
        }
        Payment payment = Payment.builder()
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
                .numberOfInstallments(dto.getNumberOfInstallments())
                .build();
        payment = paymentRepository.save(payment);
        if (status == PaymentStatus.PARTIAL && dto.getFirstDueDate() != null && dto.getNumberOfInstallments() != null && dto.getDuePeriodDays() != null) {
            generateDueDates(payment);
        }
        if (payment.getStatus() == PaymentStatus.PAID) {
            String pilgrimName = pilgrimName(payment.getPilgrimId());
            notificationProducer.notifyPaymentReceived(payment.getAgencyId(), payment.getId(), pilgrimName, payment.getAmount() + " " + payment.getCurrency());
        }
        return toDto(payment);
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

    @Transactional
    public PaymentDto update(Long id, PaymentDto dto) {
        Payment payment = findByIdAndAgency(id);
        if (dto.getAmount() != null) payment.setAmount(dto.getAmount());
        if (dto.getCurrency() != null) payment.setCurrency(dto.getCurrency());
        if (dto.getPaymentMethod() != null) payment.setPaymentMethod(dto.getPaymentMethod());
        if (dto.getStatus() != null) payment.setStatus(dto.getStatus());
        if (dto.getPaymentDate() != null) payment.setPaymentDate(dto.getPaymentDate());
        if (dto.getReference() != null) payment.setReference(dto.getReference());
        PaymentStatus before = payment.getStatus();
        payment = paymentRepository.save(payment);
        if (payment.getStatus() == PaymentStatus.PAID && before != PaymentStatus.PAID) {
            String pilgrimName = pilgrimName(payment.getPilgrimId());
            notificationProducer.notifyPaymentReceived(payment.getAgencyId(), payment.getId(), pilgrimName, payment.getAmount() + " " + payment.getCurrency());
        }
        return toDto(payment);
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
        Long agencyId = TenantContext.getAgencyId();
        if (!TenantContext.isSuperAdmin() && (agencyId == null || !agencyId.equals(payment.getAgencyId()))) {
            throw new ForbiddenException("Access denied");
        }
        if (payment.getDeletedAt() != null) throw new ResourceNotFoundException("Payment", id);
        return payment;
    }

    private PageResponse<PaymentDto> toPageResponse(Page<Payment> page) {
        List<PaymentDto> content = page.getContent().stream().map(this::toDto).collect(Collectors.toList());
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

    private PaymentDto toDto(Payment e) {
        List<PaymentDueDto> dueDtos = paymentDueRepository.findByPaymentIdOrderBySequenceOrderAsc(e.getId()).stream()
                .map(this::toDueDto)
                .collect(Collectors.toList());
        return PaymentDto.builder()
                .id(e.getId())
                .agencyId(e.getAgencyId())
                .pilgrimId(e.getPilgrimId())
                .groupId(e.getGroupId())
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
