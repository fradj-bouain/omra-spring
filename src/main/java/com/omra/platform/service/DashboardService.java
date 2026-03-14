package com.omra.platform.service;

import com.omra.platform.dto.DashboardChartDto;
import com.omra.platform.dto.DashboardGroupKpiDto;
import com.omra.platform.dto.DashboardStatsDto;
import com.omra.platform.entity.UmrahGroup;
import com.omra.platform.entity.enums.PaymentStatus;
import com.omra.platform.entity.enums.VisaStatus;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.repository.GroupPilgrimRepository;
import com.omra.platform.repository.PaymentRepository;
import com.omra.platform.repository.PilgrimRepository;
import com.omra.platform.repository.UmrahGroupRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PilgrimRepository pilgrimRepository;
    private final UmrahGroupRepository groupRepository;
    private final PaymentRepository paymentRepository;
    private final GroupPilgrimRepository groupPilgrimRepository;

    @Transactional(readOnly = true)
    public DashboardStatsDto getStats() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null && !TenantContext.isSuperAdmin()) {
            throw new ForbiddenException("Agency context required");
        }
        if (TenantContext.isSuperAdmin() && agencyId == null) {
            return DashboardStatsDto.builder()
                    .totalPilgrims(pilgrimRepository.count())
                    .activeGroups(0)
                    .pendingVisas(0)
                    .paymentsReceived(BigDecimal.ZERO)
                    .totalRevenue(BigDecimal.ZERO)
                    .build();
        }

        long totalPilgrims = pilgrimRepository.findByAgencyIdAndDeletedAtIsNull(agencyId, PageRequest.of(0, 1)).getTotalElements();
        long activeGroups = groupRepository.findByAgencyIdAndDeletedAtIsNull(agencyId, PageRequest.of(0, 1)).getTotalElements();
        long pendingVisas = pilgrimRepository.findByAgencyIdAndDeletedAtIsNull(agencyId, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent().stream()
                .filter(p -> p.getVisaStatus() == VisaStatus.PENDING || p.getVisaStatus() == VisaStatus.SUBMITTED)
                .count();
        BigDecimal paymentsReceived = paymentRepository.sumAmountByAgencyIdAndStatus(agencyId, PaymentStatus.PAID);
        BigDecimal totalRevenue = paymentsReceived != null ? paymentsReceived : BigDecimal.ZERO;

        return DashboardStatsDto.builder()
                .totalPilgrims(totalPilgrims)
                .activeGroups(activeGroups)
                .pendingVisas(pendingVisas)
                .paymentsReceived(totalRevenue)
                .totalRevenue(totalRevenue)
                .build();
    }

    @Transactional(readOnly = true)
    public List<DashboardGroupKpiDto> getGroupKpis() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null && !TenantContext.isSuperAdmin()) {
            throw new ForbiddenException("Agency context required");
        }
        if (TenantContext.isSuperAdmin() && agencyId == null) {
            return List.of();
        }
        List<UmrahGroup> groups = groupRepository.findByAgencyIdAndDeletedAtIsNull(agencyId, PageRequest.of(0, 500)).getContent();
        List<DashboardGroupKpiDto> result = new ArrayList<>();
        for (UmrahGroup g : groups) {
            int filled = (int) groupPilgrimRepository.countByGroupId(g.getId());
            BigDecimal totalPaid = paymentRepository.sumAmountByGroupIdAndStatus(g.getId(), PaymentStatus.PAID);
            if (totalPaid == null) totalPaid = BigDecimal.ZERO;
            result.add(DashboardGroupKpiDto.builder()
                    .groupId(g.getId())
                    .groupName(g.getName())
                    .filledCapacity(filled)
                    .maxCapacity(g.getMaxCapacity() != null ? g.getMaxCapacity() : 0)
                    .totalPaid(totalPaid)
                    .price(g.getPrice() != null ? g.getPrice() : BigDecimal.ZERO)
                    .status(g.getStatus() != null ? g.getStatus().name() : null)
                    .build());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public DashboardChartDto getChartData() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null && !TenantContext.isSuperAdmin()) {
            throw new ForbiddenException("Agency context required");
        }
        if (TenantContext.isSuperAdmin() && agencyId == null) {
            return DashboardChartDto.builder()
                    .paymentsOverTime(List.of())
                    .visaDistribution(List.of())
                    .build();
        }
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusMonths(12);
        List<com.omra.platform.entity.Payment> payments = paymentRepository.findByAgencyIdAndStatusAndDeletedAtIsNullAndPaymentDateBetween(
                agencyId, PaymentStatus.PAID, start, end);
        Map<String, BigDecimal> byPeriod = payments.stream()
                .filter(p -> p.getPaymentDate() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getPaymentDate().getYear() + "-" + String.format("%02d", p.getPaymentDate().getMonthValue()),
                        Collectors.reducing(BigDecimal.ZERO, com.omra.platform.entity.Payment::getAmount, BigDecimal::add)));
        List<DashboardChartDto.PeriodAmountDto> paymentsOverTime = byPeriod.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new DashboardChartDto.PeriodAmountDto(e.getKey(), e.getValue()))
                .toList();
        List<com.omra.platform.entity.Pilgrim> pilgrims = pilgrimRepository.findByAgencyIdAndDeletedAtIsNull(agencyId, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        Map<VisaStatus, Long> visaCounts = pilgrims.stream().collect(Collectors.groupingBy(p -> p.getVisaStatus() != null ? p.getVisaStatus() : VisaStatus.PENDING, Collectors.counting()));
        List<DashboardChartDto.StatusCountDto> visaDistribution = visaCounts.entrySet().stream()
                .map(e -> new DashboardChartDto.StatusCountDto(e.getKey().name(), e.getValue()))
                .toList();
        return DashboardChartDto.builder()
                .paymentsOverTime(paymentsOverTime)
                .visaDistribution(visaDistribution)
                .build();
    }
}
