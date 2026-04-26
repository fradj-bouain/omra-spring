-- Kind of agency tenant (travel vs marketplace vs hotel operator).
ALTER TABLE agencies
    ADD COLUMN IF NOT EXISTS agency_kind VARCHAR(16) NOT NULL DEFAULT 'TRAVEL';

COMMENT ON COLUMN agencies.agency_kind IS 'TRAVEL | MARKETPLACE | HOTEL — drives available agency modules';
