package com.omra.platform.service;

import com.omra.platform.entity.UiTranslation;
import com.omra.platform.repository.UiTranslationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UiTranslationService {

    private static final String DEFAULT_LOCALE = "fr";

    private final UiTranslationRepository uiTranslationRepository;

    @Transactional(readOnly = true)
    public Map<String, String> getMapForLocale(String locale) {
        String normalized = normalizeLocale(locale);
        List<UiTranslation> rows = uiTranslationRepository.findByLocaleOrderByMsgKeyAsc(normalized);
        Map<String, String> map = new LinkedHashMap<>();
        for (UiTranslation row : rows) {
            map.put(row.getMsgKey(), row.getMsgValue());
        }
        return map;
    }

    /** Accepts "fr", "ar", "fr-FR" → "fr" | "ar" */
    public static String normalizeLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return DEFAULT_LOCALE;
        }
        String l = locale.trim().toLowerCase(Locale.ROOT);
        if (l.startsWith("ar")) {
            return "ar";
        }
        return "fr";
    }
}
