-- Footer: branding FLUTTIRIS (remplace © année)
INSERT INTO ui_translations (locale, msg_key, msg_value) VALUES
('fr', 'layout.footer', 'Créé par FLUTTIRIS'),
('ar', 'layout.footer', 'Created by FLUTTIRIS')
ON CONFLICT (locale, msg_key) DO UPDATE SET msg_value = EXCLUDED.msg_value;
