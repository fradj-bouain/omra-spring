-- Nom et contact du chauffeur pour les bus
ALTER TABLE buses ADD COLUMN IF NOT EXISTS driver_name VARCHAR(128);
ALTER TABLE buses ADD COLUMN IF NOT EXISTS driver_contact VARCHAR(64);
