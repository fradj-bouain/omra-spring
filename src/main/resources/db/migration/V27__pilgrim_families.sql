-- Regroupement « famille » pour plusieurs pèlerins créés ensemble (mo3tamir famille).
CREATE TABLE pilgrim_families (
    id         BIGSERIAL PRIMARY KEY,
    agency_id  BIGINT NOT NULL REFERENCES agencies (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_pilgrim_families_agency ON pilgrim_families (agency_id);

ALTER TABLE pilgrims
    ADD COLUMN IF NOT EXISTS family_id BIGINT REFERENCES pilgrim_families (id) ON DELETE SET NULL;

ALTER TABLE pilgrims
    ADD COLUMN IF NOT EXISTS family_role VARCHAR(20);

CREATE INDEX IF NOT EXISTS idx_pilgrims_family_id ON pilgrims (family_id);
