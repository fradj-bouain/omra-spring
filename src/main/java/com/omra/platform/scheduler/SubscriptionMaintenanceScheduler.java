package com.omra.platform.scheduler;

import com.omra.platform.entity.Agency;
import com.omra.platform.entity.AgencySubscription;
import com.omra.platform.entity.enums.AgencyStatus;
import com.omra.platform.entity.enums.AgencySubscriptionStatus;
import com.omra.platform.repository.AgencyRepository;
import com.omra.platform.repository.AgencySubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Passe les abonnements expirés en {@link AgencySubscriptionStatus#EXPIRED} et suspend les agences sans couverture payante.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionMaintenanceScheduler {

    private final AgencySubscriptionRepository agencySubscriptionRepository;
    private final AgencyRepository agencyRepository;

    @Scheduled(cron = "${app.subscription.maintenance-cron:0 0 3 * * *}")
    @Transactional
    public void expireSubscriptions() {
        LocalDate today = LocalDate.now();
        Set<Long> touchedAgencies = new HashSet<>();

        for (AgencySubscriptionStatus st : List.of(AgencySubscriptionStatus.ACTIVE, AgencySubscriptionStatus.PENDING_PAYMENT)) {
            List<AgencySubscription> overdue = agencySubscriptionRepository.findByStatusAndPeriodEndBefore(st, today);
            for (AgencySubscription s : overdue) {
                if (s.getStatus() == AgencySubscriptionStatus.ACTIVE || s.getStatus() == AgencySubscriptionStatus.PENDING_PAYMENT) {
                    s.setStatus(AgencySubscriptionStatus.EXPIRED);
                    agencySubscriptionRepository.save(s);
                    touchedAgencies.add(s.getAgencyId());
                }
            }
        }

        for (Long agencyId : touchedAgencies) {
            syncAgencySuspension(agencyId, today);
        }

        if (!touchedAgencies.isEmpty()) {
            log.info("Abonnements expirés traités pour {} agence(s)", touchedAgencies.size());
        }
    }

    private void syncAgencySuspension(Long agencyId, LocalDate today) {
        boolean hasValid = !agencySubscriptionRepository
                .findValidPaidCovering(agencyId, AgencySubscriptionStatus.ACTIVE, today, PageRequest.of(0, 1))
                .isEmpty();
        if (hasValid) {
            return;
        }
        Agency agency = agencyRepository.findById(agencyId).orElse(null);
        if (agency != null && agency.getStatus() == AgencyStatus.ACTIVE) {
            agency.setStatus(AgencyStatus.SUSPENDED);
            agencyRepository.save(agency);
            log.info("Agence {} suspendue : aucun abonnement payé valide", agencyId);
        }
    }
}
