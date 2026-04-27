package com.omra.platform.entity.enums;

/** How transport rental price is quoted on an offer. */
public enum TransportPricingUnit {
    /** Fixed price per calendar day in the rental window. */
    DAY,
    /** Price per hour (total depends on duration negotiated / desired window). */
    HOUR,
    /** Flat price per trip / displacement. */
    TRIP,
}
