package com.omra.platform.entity.enums;

/**
 * User roles: SUPER_ADMIN (platform); within an agency: AGENCY_ADMIN (admin),
 * AGENCY_AGENT (service), PILGRIM_COMPANION (Pilgrimage companion); PILGRIM (pilgrim portal).
 */
public enum UserRole {
    SUPER_ADMIN,
    AGENCY_ADMIN,      // agency admin
    AGENCY_AGENT,      // service
    PILGRIM_COMPANION, // Pilgrimage companion
    PILGRIM
}
