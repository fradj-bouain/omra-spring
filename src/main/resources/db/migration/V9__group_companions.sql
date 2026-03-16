-- Assignation d'accompagnateurs (rôle PILGRIM_COMPANION) aux groupes Omra
CREATE TABLE IF NOT EXISTS group_companions (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES umrah_groups(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(group_id, user_id)
);
CREATE INDEX IF NOT EXISTS idx_group_companion_group ON group_companions(group_id);
CREATE INDEX IF NOT EXISTS idx_group_companion_user ON group_companions(user_id);
