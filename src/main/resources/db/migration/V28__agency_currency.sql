-- Devise par agence (ISO 4217, ex. MAD, EUR).
ALTER TABLE agencies
    ADD COLUMN IF NOT EXISTS currency VARCHAR(8);
