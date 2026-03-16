-- Types de tâches prédéfinis (Tawaf, Sa'i, etc.) avec durée
CREATE TABLE IF NOT EXISTS task_templates (
    id BIGSERIAL PRIMARY KEY,
    agency_id BIGINT NOT NULL REFERENCES agencies(id) ON DELETE CASCADE,
    name VARCHAR(128) NOT NULL,
    duration_minutes INT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_task_template_agency ON task_templates(agency_id);

-- Plannings = modèles de programme (liste ordonnée de tâches)
CREATE TABLE IF NOT EXISTS plannings (
    id BIGSERIAL PRIMARY KEY,
    agency_id BIGINT NOT NULL REFERENCES agencies(id) ON DELETE CASCADE,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_planning_agency ON plannings(agency_id);

-- Éléments d'un planning (ordre des tâches)
CREATE TABLE IF NOT EXISTS planning_items (
    id BIGSERIAL PRIMARY KEY,
    planning_id BIGINT NOT NULL REFERENCES plannings(id) ON DELETE CASCADE,
    task_template_id BIGINT NOT NULL REFERENCES task_templates(id) ON DELETE CASCADE,
    sort_order INT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_planning_item_planning ON planning_items(planning_id);

-- Groupe Omra peut avoir un planning assigné
ALTER TABLE umrah_groups ADD COLUMN IF NOT EXISTS planning_id BIGINT REFERENCES plannings(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_umrah_group_planning ON umrah_groups(planning_id);
