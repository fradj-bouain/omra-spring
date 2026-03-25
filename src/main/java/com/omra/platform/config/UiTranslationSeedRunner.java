package com.omra.platform.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Remplit / met à jour {@code ui_translations} au démarrage à partir de
 * {@code classpath:i18n/ui-translations.seed.json} (généré par {@code node tools/generate-ui-translations.mjs}).
 * <p>
 * Sans Flyway, la migration V18 n'est pas exécutée : seule Hibernate crée la table, d'où une API vide.
 * Ce seeder garantit des données à jour à chaque déploiement.
 */
@Component
@Order(15)
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.i18n.seed.enabled", havingValue = "true", matchIfMissing = true)
public class UiTranslationSeedRunner implements ApplicationRunner {

    private static final Resource SEED = new ClassPathResource("i18n/ui-translations.seed.json");

    private static final String UPSERT_PG = """
            INSERT INTO ui_translations (locale, msg_key, msg_value)
            VALUES (?, ?, ?)
            ON CONFLICT (locale, msg_key) DO UPDATE SET msg_value = EXCLUDED.msg_value
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (!SEED.exists()) {
            log.warn("Fichier i18n/ui-translations.seed.json absent — exécutez: node tools/generate-ui-translations.mjs");
            return;
        }
        List<Object[]> batch = new ArrayList<>();
        try (InputStream in = SEED.getInputStream()) {
            JsonNode root = objectMapper.readTree(in);
            JsonNode entries = root.get("entries");
            if (entries == null || !entries.isArray()) {
                log.warn("ui-translations.seed.json: champ 'entries' manquant ou invalide");
                return;
            }
            for (JsonNode row : entries) {
                String locale = text(row, "locale");
                String msgKey = text(row, "msgKey");
                String msgValue = text(row, "msgValue");
                if (locale == null || msgKey == null || msgValue == null) {
                    continue;
                }
                batch.add(new Object[] {locale, msgKey, msgValue});
            }
        }
        if (batch.isEmpty()) {
            log.warn("Aucune entrée i18n à insérer (seed vide)");
            return;
        }
        jdbcTemplate.batchUpdate(UPSERT_PG, batch);
        log.info("i18n: {} traductions upsert (fr+ar) depuis ui-translations.seed.json", batch.size());
    }

    private static String text(JsonNode row, String field) {
        JsonNode n = row.get(field);
        if (n == null || n.isNull()) {
            return null;
        }
        return n.asText();
    }
}
