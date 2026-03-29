package com.omra.platform.service;

import com.omra.platform.dto.DashboardChartDto;
import com.omra.platform.dto.DashboardGroupKpiDto;
import com.omra.platform.dto.DashboardStatsDto;
import com.omra.platform.entity.UmrahGroup;
import com.omra.platform.entity.enums.AgencyStatus;
import com.omra.platform.entity.enums.PaymentStatus;
import com.omra.platform.entity.enums.VisaStatus;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.repository.AgencyRepository;
import com.omra.platform.repository.GroupPilgrimRepository;
import com.omra.platform.repository.PaymentRepository;
import com.omra.platform.repository.PilgrimRepository;
import com.omra.platform.repository.UmrahGroupRepository;
import com.omra.platform.repository.UserRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
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
    private final AgencyRepository agencyRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public DashboardStatsDto getStats() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null && !TenantContext.isSuperAdmin()) {
            throw new ForbiddenException("Agency context required");
        }
        if (TenantContext.isSuperAdmin() && agencyId == null) {
            BigDecimal paidGlobal = paymentRepository.sumAmountByStatus(PaymentStatus.PAID);
            if (paidGlobal == null) {
                paidGlobal = BigDecimal.ZERO;
            }
            long pendingVisasGlobal = pilgrimRepository.countByDeletedAtIsNullAndVisaStatusIn(
                    EnumSet.of(VisaStatus.PENDING, VisaStatus.SUBMITTED));
            return DashboardStatsDto.builder()
                    .totalPilgrims(pilgrimRepository.countByDeletedAtIsNull())
                    .activeGroups(groupRepository.countByDeletedAtIsNull())
                    .pendingVisas(pendingVisasGlobal)
                    .paymentsReceived(paidGlobal)
                    .totalRevenue(paidGlobal)
                    .totalAgencies(agencyRepository.count())
                    .activeAgencies(agencyRepository.countByStatus(AgencyStatus.ACTIVE))
                    .suspendedAgencies(agencyRepository.countByStatus(AgencyStatus.SUSPENDED))
                    .expiredAgencies(agencyRepository.countByStatus(AgencyStatus.EXPIRED))
                    .totalAgencyUsers(userRepository.countByAgencyIdIsNotNullAndDeletedAtIsNull())
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
                .totalAgencies(0)
                .activeAgencies(0)
                .suspendedAgencies(0)
                .expiredAgencies(0)
                .totalAgencyUsers(0)
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
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusMonths(12);
            List<com.omra.platform.entity.Payment> paymentsGlobal =
                    paymentRepository.findByStatusAndDeletedAtIsNullAndPaymentDateBetween(PaymentStatus.PAID, start, end);
            Map<String, BigDecimal> byPeriod = paymentsGlobal.stream()
                    .filter(p -> p.getPaymentDate() != null)
                    .collect(Collectors.groupingBy(
                            p -> p.getPaymentDate().getYear() + "-" + String.format("%02d", p.getPaymentDate().getMonthValue()),
                            Collectors.reducing(BigDecimal.ZERO, com.omra.platform.entity.Payment::getAmount, BigDecimal::add)));
            List<DashboardChartDto.PeriodAmountDto> paymentsOverTime = byPeriod.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> new DashboardChartDto.PeriodAmountDto(e.getKey(), e.getValue()))
                    .toList();
            List<com.omra.platform.entity.Pilgrim> pilgrims =
                    pilgrimRepository.findByDeletedAtIsNull(PageRequest.of(0, Integer.MAX_VALUE)).getContent();
            Map<VisaStatus, Long> visaCounts = pilgrims.stream()
                    .collect(Collectors.groupingBy(
                            p -> p.getVisaStatus() != null ? p.getVisaStatus() : VisaStatus.PENDING,
                            Collectors.counting()));
            List<DashboardChartDto.StatusCountDto> visaDistribution = visaCounts.entrySet().stream()
                    .map(e -> new DashboardChartDto.StatusCountDto(e.getKey().name(), e.getValue()))
                    .toList();
            return DashboardChartDto.builder()
                    .paymentsOverTime(paymentsOverTime)
                    .visaDistribution(visaDistribution)
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
