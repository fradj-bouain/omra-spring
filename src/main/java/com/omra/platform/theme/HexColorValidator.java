package com.omra.platform.theme;

import com.omra.platform.exception.BadRequestException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Valide et normalise les couleurs hex (#RGB ou #RRGGBB).
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HexColorValidator {

    private static final Pattern HEX6 = Pattern.compile("^#([0-9A-Fa-f]{6})$");
    private static final Pattern HEX3 = Pattern.compile("^#([0-9A-Fa-f]{3})$");

    /** Normalise ou lève {@link BadRequestException} si invalide. */
    public static String normalizeOrThrow(String input, String fieldName) {
        if (input == null || input.isBlank()) {
            throw new BadRequestException(fieldName + " is required");
        }
        String s = input.trim();
        if (!s.startsWith("#")) {
            s = "#" + s;
        }
        var m3 = HEX3.matcher(s);
        if (m3.matches()) {
            String g = m3.group(1);
            s = "#" + g.charAt(0) + g.charAt(0) + g.charAt(1) + g.charAt(1) + g.charAt(2) + g.charAt(2);
        }
        if (!HEX6.matcher(s).matches()) {
            throw new BadRequestException("Invalid HEX color for " + fieldName + ": use #RRGGBB");
        }
        return s.toUpperCase(Locale.ROOT);
    }

    /** Si null ou vide, retourne null ; sinon normalise ou lève. */
    public static String optionalNormalizeOrThrow(String input, String fieldName) {
        if (input == null || input.isBlank()) {
            return null;
        }
        return normalizeOrThrow(input, fieldName);
    }
}
