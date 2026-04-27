-- Hotel offers publication status (active vs disabled).
ALTER TABLE hotel_offers
    ADD COLUMN IF NOT EXISTS status VARCHAR(16) NOT NULL DEFAULT 'DISABLED';

CREATE INDEX IF NOT EXISTS idx_hotel_offers_status ON hotel_offers (status);

