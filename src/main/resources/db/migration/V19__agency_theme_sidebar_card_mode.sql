-- Thème agence : barre latérale, cartes, mode clair/sombre
ALTER TABLE agencies ADD COLUMN IF NOT EXISTS sidebar_color VARCHAR(16);
ALTER TABLE agencies ADD COLUMN IF NOT EXISTS card_color VARCHAR(16);
ALTER TABLE agencies ADD COLUMN IF NOT EXISTS theme_mode VARCHAR(16);

UPDATE agencies SET sidebar_color = menu_color WHERE sidebar_color IS NULL AND menu_color IS NOT NULL AND trim(menu_color) <> '';
UPDATE agencies SET sidebar_color = '#0F172A' WHERE sidebar_color IS NULL OR trim(sidebar_color) = '';

UPDATE agencies SET menu_color = sidebar_color WHERE menu_color IS NULL OR trim(menu_color) = '';

UPDATE agencies SET card_color = '#FFFFFF' WHERE card_color IS NULL OR trim(card_color) = '';

UPDATE agencies SET primary_color = '#2563EB' WHERE primary_color IS NULL OR trim(primary_color) = '';
UPDATE agencies SET secondary_color = '#1E293B' WHERE secondary_color IS NULL OR trim(secondary_color) = '';
UPDATE agencies SET button_color = primary_color WHERE button_color IS NULL OR trim(button_color) = '';
UPDATE agencies SET background_color = '#F8FAFC' WHERE background_color IS NULL OR trim(background_color) = '';
UPDATE agencies SET text_color = '#111827' WHERE text_color IS NULL OR trim(text_color) = '';

UPDATE agencies SET theme_mode = 'LIGHT' WHERE theme_mode IS NULL OR trim(theme_mode) = '';
