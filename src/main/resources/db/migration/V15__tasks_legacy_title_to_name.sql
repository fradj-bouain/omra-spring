-- L'entité Task attend la colonne `name`. L'ancien schéma (V2) utilisait `title`.
-- Si V10 n'a pas été appliqué ou si la table a été conservée / modifiée hors Flyway,
-- Hibernate ou les requêtes échouent avec "column ... name does not exist".

DO
$$
    BEGIN
        IF to_regclass('public.tasks') IS NULL THEN
            -- Rien à faire (V14 créera la table sur une base neuve).
            NULL;
        ELSE
            -- Cas 1 : seulement `title` (legacy V2)
            IF EXISTS(SELECT 1
                      FROM information_schema.columns
                      WHERE table_schema = 'public'
                        AND table_name = 'tasks'
                        AND column_name = 'title')
                AND NOT EXISTS(SELECT 1
                               FROM information_schema.columns
                               WHERE table_schema = 'public'
                                 AND table_name = 'tasks'
                                 AND column_name = 'name') THEN
                ALTER TABLE tasks RENAME COLUMN title TO name;
            END IF;

            -- Cas 2 : `title` et `name` présents (état incohérent)
            IF EXISTS(SELECT 1
                      FROM information_schema.columns
                      WHERE table_schema = 'public'
                        AND table_name = 'tasks'
                        AND column_name = 'title')
                AND EXISTS(SELECT 1
                           FROM information_schema.columns
                           WHERE table_schema = 'public'
                             AND table_name = 'tasks'
                             AND column_name = 'name') THEN
                UPDATE tasks
                SET name = COALESCE(NULLIF(TRIM(name), ''), title);
                ALTER TABLE tasks DROP COLUMN title;
            END IF;

            -- Cas 3 : pas de `name` (ni title) — ajout minimal
            IF NOT EXISTS(SELECT 1
                          FROM information_schema.columns
                          WHERE table_schema = 'public'
                            AND table_name = 'tasks'
                            AND column_name = 'name') THEN
                ALTER TABLE tasks
                    ADD COLUMN name VARCHAR(255) NOT NULL DEFAULT 'Tâche';
                ALTER TABLE tasks
                    ALTER COLUMN name DROP DEFAULT;
            END IF;
        END IF;
    END
$$;
