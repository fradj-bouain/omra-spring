package com.omra.platform.theme;

import com.omra.platform.dto.AgencyDto;
import com.omra.platform.entity.Agency;
import com.omra.platform.entity.enums.ThemeMode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Palette « Blue SaaS » appliquée à la création d’agence et comme repli si des champs sont vides.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AgencyThemeDefaults {

    public static final String PRIMARY = "#2563EB";
    public static final String SECONDARY = "#1E293B";
    public static final String SIDEBAR = "#0F172A";
    public static final String BACKGROUND = "#F8FAFC";
    public static final String CARD = "#FFFFFF";
    public static final String TEXT = "#111827";
    /** Couleur des boutons / accent actions (alignée sur primaire par défaut). */
    public static final String BUTTON = PRIMARY;

    /**
     * Remplit les champs thème manquants (null ou blanc). Retourne true si l’entité a été modifiée.
     */
    public static boolean fillMissingThemeFields(Agency agency) {
        if (agency == null) {
            return false;
        }
        boolean changed = false;
        if (isBlank(agency.getPrimaryColor())) {
            agency.setPrimaryColor(PRIMARY);
            changed = true;
        }
        if (isBlank(agency.getSecondaryColor())) {
            agency.setSecondaryColor(SECONDARY);
            changed = true;
        }
        if (isBlank(agency.getSidebarColor())) {
            String fallback = isBlank(agency.getMenuColor()) ? SIDEBAR : agency.getMenuColor();
            agency.setSidebarColor(fallback);
            changed = true;
        }
        if (isBlank(agency.getMenuColor())) {
            agency.setMenuColor(agency.getSidebarColor());
            changed = true;
        }
        if (isBlank(agency.getButtonColor())) {
            agency.setButtonColor(BUTTON);
            changed = true;
        }
        if (isBlank(agency.getBackgroundColor())) {
            agency.setBackgroundColor(BACKGROUND);
            changed = true;
        }
        if (isBlank(agency.getCardColor())) {
            agency.setCardColor(CARD);
            changed = true;
        }
        if (isBlank(agency.getTextColor())) {
            agency.setTextColor(TEXT);
            changed = true;
        }
        if (agency.getThemeMode() == null) {
            agency.setThemeMode(ThemeMode.LIGHT);
            changed = true;
        }
        return changed;
    }

    /**
     * Création : fusionne le DTO avec les défauts (couleurs optionnelles côté client).
     */
    public static void applyThemeOnCreate(Agency agency, AgencyDto dto) {
        agency.setPrimaryColor(requireColor(dto != null ? dto.getPrimaryColor() : null, PRIMARY));
        agency.setSecondaryColor(requireColor(dto != null ? dto.getSecondaryColor() : null, SECONDARY));
        String sidebar = optionalColor(dto != null ? dto.getSidebarColor() : null, "sidebarColor");
        if (sidebar == null) {
            sidebar = requireColor(dto != null ? dto.getMenuColor() : null, SIDEBAR);
        }
        agency.setSidebarColor(sidebar);
        agency.setMenuColor(requireColor(dto != null ? dto.getMenuColor() : null, sidebar));
        agency.setButtonColor(requireColor(dto != null ? dto.getButtonColor() : null, BUTTON));
        agency.setBackgroundColor(requireColor(dto != null ? dto.getBackgroundColor() : null, BACKGROUND));
        agency.setCardColor(requireColor(dto != null ? dto.getCardColor() : null, CARD));
        agency.setTextColor(requireColor(dto != null ? dto.getTextColor() : null, TEXT));
        if (dto != null && dto.getThemeMode() != null) {
            agency.setThemeMode(dto.getThemeMode());
        } else {
            agency.setThemeMode(ThemeMode.LIGHT);
        }
    }

    private static String optionalColor(String fromDto, String field) {
        if (isBlank(fromDto)) {
            return null;
        }
        return HexColorValidator.normalizeOrThrow(fromDto, field);
    }

    private static String requireColor(String fromDto, String fallback) {
        if (!isBlank(fromDto)) {
            return HexColorValidator.normalizeOrThrow(fromDto, "theme color");
        }
        return fallback;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
