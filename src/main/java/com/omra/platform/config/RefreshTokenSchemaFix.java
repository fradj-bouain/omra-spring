package com.omra.platform.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * One-time fix: allow NULL on refresh_tokens.user_id so admin refresh tokens
 * (with only admin_id set) can be stored. Runs at startup after the context is ready.
 * If this fails, run manually: src/main/resources/db/refresh_tokens_fix.sql
 */
@Component
@Order(1)
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenSchemaFix implements ApplicationRunner {

    private static final String SQL = "ALTER TABLE refresh_tokens ALTER COLUMN user_id DROP NOT NULL";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute(SQL);
            log.info("Schema fix applied: refresh_tokens.user_id is now nullable (admin tokens support)");
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("already") || msg.contains("is already nullable")) {
                log.debug("RefreshToken schema fix already applied: {}", msg);
            } else {
                log.error("Schema fix failed. Run this SQL manually on your database: {} - Error: {}", SQL, msg);
            }
        }
    }
}
