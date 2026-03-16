-- Hôtel : pays, contact important, réception, email
ALTER TABLE hotels ADD COLUMN IF NOT EXISTS country VARCHAR(128);
ALTER TABLE hotels ADD COLUMN IF NOT EXISTS contact_important VARCHAR(255);
ALTER TABLE hotels ADD COLUMN IF NOT EXISTS reception_phone VARCHAR(64);
ALTER TABLE hotels ADD COLUMN IF NOT EXISTS email VARCHAR(255);
-- Si la table avait contactPhone en un seul mot, standardiser le nom de colonne (optionnel)
-- Ici on suppose que la colonne existante est contact_phone (JPA snake_case)
