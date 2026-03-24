-- Hierarchical tasks (self-referencing). CASCADE deletes descendants in DB.
CREATE TABLE tasks (
    id              BIGSERIAL PRIMARY KEY,
    agency_id       BIGINT NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    duration_minutes INTEGER,
    parent_id       BIGINT REFERENCES tasks (id) ON DELETE CASCADE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tasks_agency_id ON tasks (agency_id);
CREATE INDEX idx_tasks_parent_id ON tasks (parent_id);
