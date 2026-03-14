-- Allow NULL on refresh_tokens.user_id so admin refresh tokens (admin_id only) can be stored.
-- Run this once if the application fails with: null value in column "user_id" violates not-null constraint
-- Example: psql -U postgres -d omra_db -f src/main/resources/db/refresh_tokens_fix.sql

ALTER TABLE refresh_tokens ALTER COLUMN user_id DROP NOT NULL;
