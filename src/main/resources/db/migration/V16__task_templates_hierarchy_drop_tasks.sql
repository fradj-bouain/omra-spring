-- Supprime la table tasks (remplacée par la hiérarchie dans task_templates).
DROP TABLE IF EXISTS tasks CASCADE;

-- Hiérarchie : sous-types de tâches (ex: Tawaf > Préparation).
ALTER TABLE task_templates
    ADD COLUMN IF NOT EXISTS parent_id BIGINT;

ALTER TABLE task_templates
    ADD COLUMN IF NOT EXISTS description TEXT;

DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1
                       FROM pg_constraint
                       WHERE conname = 'task_templates_parent_id_fkey') THEN
            ALTER TABLE task_templates
                ADD CONSTRAINT task_templates_parent_id_fkey
                    FOREIGN KEY (parent_id) REFERENCES task_templates (id) ON DELETE SET NULL;
        END IF;
    END
$$;

CREATE INDEX IF NOT EXISTS idx_task_template_parent ON task_templates (parent_id);
