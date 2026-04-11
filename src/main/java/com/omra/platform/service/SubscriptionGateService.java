package com.omra.platform.service;

import com.omra.platform.entity.Agency;
import com.omra.platform.entity.AgencySubscription;
import com.omra.platform.entity.enums.AgencyStatus;
import com.omra.platform.entity.enums.AgencySubscriptionStatus;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.repository.AgencyRepository;
import com.omra.platform.repository.AgencySubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrôle d'accès des utilisateurs d'agence : agence {@link AgencyStatus#ACTIVE}
 * + au moins un abonnement payé couvrant la date du jour.
 */
@Service
@RequiredArgsConstructor
public class SubscriptionGateService {

    private final AgencyRepository agencyRepository;
    private final AgencySubscriptionRepository agencySubscriptionRepository;

    @Transactional(readOnly = true)
    public void assertAgencyUsersMayAuthenticate(Long agencyId) {
        if (agencyId == null) {
            return;
        }
        Agency agency = agencyRepository.findById(agencyId).orElseThrow(() -> new ForbiddenException("Agence introuvable."));
        if (agency.getStatus() != AgencyStatus.ACTIVE) {
            throw new ForbiddenException(
                    "Votre agence est suspendue ou désactivée. Contactez l'administrateur de la plateforme.",
                    "AGENCY_SUSPENDED");
        }
        Long billingAgencyId = agency.getParentAgencyId() != null ? agency.getParentAgencyId() : agencyId;
        if (agency.getParentAgencyId() != null) {
            Agency main = agencyRepository.findById(agency.getParentAgencyId())
                    .orElseThrow(() -> new ForbiddenException("Agence principale introuvable.", "PARENT_AGENCY_MISSING"));
            if (main.getStatus() != AgencyStatus.ACTIVE) {
                throw new ForbiddenException(
                        "L'agence principale est suspendue. Contactez votre administrateur.",
                        "PARENT_AGENCY_SUSPENDED");
            }
        }
        List<AgencySubscription> valid = agencySubscriptionRepository.findValidPaidCovering(
                billingAgencyId, AgencySubscriptionStatus.ACTIVE, LocalDate.now(), PageRequest.of(0, 1));
        if (valid.isEmpty()) {
            throw new ForbiddenException(
                    "Votre abonnement est terminé, expiré ou annulé. Renouvelez-le auprès de l'administrateur de la plateforme pour retrouver l'accès.",
                    "SUBSCRIPTION_INACTIVE");
        }
    }

    @Transactional(readOnly = true)
    public boolean hasValidPaidSubscription(Long agencyId) {
        if (agencyId == null) {
            return true;
        }
        Agency agency = agencyRepository.findById(agencyId).orElse(null);
        if (agency == null) {
            return false;
        }
        Long billingAgencyId = agency.getParentAgencyId() != null ? agency.getParentAgencyId() : agencyId;
        return !agencySubscriptionRepository
                .findValidPaidCovering(billingAgencyId, AgencySubscriptionStatus.ACTIVE, LocalDate.now(), PageRequest.of(0, 1))
                .isEmpty();
    }
}
