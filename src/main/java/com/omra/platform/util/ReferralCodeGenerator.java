package com.omra.platform.util;

import java.security.SecureRandom;

/**
 * Generates short, readable, unique referral codes (e.g. ABC12XYZ).
 * Uses alphanumeric excluding ambiguous chars (0/O, 1/I/L).
 */
public final class ReferralCodeGenerator {

    private static final String CHARS = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
    private static final int LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private ReferralCodeGenerator() {}

    public static String generate() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
