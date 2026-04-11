-- Libellé type PILGRIM sans référence omra/hajj explicite (FR + AR)
INSERT INTO ui_translations (locale, msg_key, msg_value) VALUES
('fr', 'pilgrims.travelerType.PILGRIM', 'Voyage religieux'),
('ar', 'pilgrims.travelerType.PILGRIM', 'سفر ديني')
ON CONFLICT (locale, msg_key) DO UPDATE SET msg_value = EXCLUDED.msg_value;
