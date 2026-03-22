-- Hajj vs Omra for groups (mobile & display)
ALTER TABLE umrah_groups ADD COLUMN trip_type VARCHAR(16) NOT NULL DEFAULT 'OMRRA';
