-- Libellés des sections du menu latéral (agence)
INSERT INTO ui_translations (locale, msg_key, msg_value) VALUES
('fr', 'nav.group.participants', 'Pèlerins & groupes'),
('ar', 'nav.group.participants', 'الحجاج والمجموعات'),
('fr', 'nav.group.travelStay', 'Voyage & séjour'),
('ar', 'nav.group.travelStay', 'السفر والإقامة'),
('fr', 'nav.group.administrative', 'Dossier & finances'),
('ar', 'nav.group.administrative', 'الملف والمالية'),
('fr', 'nav.group.operations', 'Organisation'),
('ar', 'nav.group.operations', 'التنظيم'),
('fr', 'nav.group.agency', 'Mon agence'),
('ar', 'nav.group.agency', 'وكالتي')
ON CONFLICT (locale, msg_key) DO UPDATE SET msg_value = EXCLUDED.msg_value;
