package com.omra.platform.entity.enums;

/**
 * Business profile of an agency tenant. Drives which modules are available in the agency UI.
 */
public enum AgencyKind {
    /** Omra / travel agency (default): pilgrims, groups, trip logistics, etc. */
    TRAVEL,
    /** E-commerce tenant: catalog, orders, stock for one storefront per agency. */
    MARKETPLACE,
    /** Hotel operator: own hotel listings and promotional offers (pricing per person, room, or group). */
    HOTEL,
    /** Transport carrier: fleet (buses, cars) and rental offers (per day, hour, or trip). */
    TRANSPORT,
}
