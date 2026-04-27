-- Soft delete: éviter les DELETE physiques (FK / réservations / commandes).
ALTER TABLE hotel_properties
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

ALTER TABLE hotel_offers
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

ALTER TABLE marketplace_products
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_hotel_properties_agency_not_deleted
    ON hotel_properties (agency_id) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_hotel_offers_agency_not_deleted
    ON hotel_offers (agency_id) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_marketplace_products_agency_not_deleted
    ON marketplace_products (agency_id) WHERE deleted_at IS NULL;
