package com.omra.platform.entity.enums;

public enum AgencySubscriptionStatus {
    /** En attente de paiement : accès agence bloqué */
    PENDING_PAYMENT,
    /** Payé et dans la période : accès autorisé si {@code Agency.status} = ACTIVE */
    ACTIVE,
    /** Période dépassée */
    EXPIRED,
    /** Remplacé ou annulé manuellement */
    CANCELLED
}
