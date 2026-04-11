-- Single-level hierarchy: main agency (parent_agency_id IS NULL) → sub-agencies only.
ALTER TABLE agencies ADD COLUMN IF NOT EXISTS parent_agency_id BIGINT REFERENCES agencies(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_agencies_parent_agency_id ON agencies(parent_agency_id);
