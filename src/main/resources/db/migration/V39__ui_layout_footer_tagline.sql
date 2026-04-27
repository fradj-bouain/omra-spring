INSERT INTO ui_translations (locale, msg_key, msg_value) VALUES
('fr', 'layout.footerTagline', 'Ingénierie logicielle & solutions professionnelles pour l’hôtellerie et le voyage'),
('ar', 'layout.footerTagline', 'هندسة برمجيات وحلول مهنية للفنادق والسفر')
ON CONFLICT (locale, msg_key) DO UPDATE SET msg_value = EXCLUDED.msg_value;
