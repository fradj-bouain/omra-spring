-- Reservation requests from TRAVEL agencies on HOTEL offers.
CREATE TABLE IF NOT EXISTS hotel_offer_reservations (
    id BIGSERIAL PRIMARY KEY,
    offer_id BIGINT NOT NULL REFERENCES hotel_offers(id) ON DELETE CASCADE,
    hotel_agency_id BIGINT NOT NULL REFERENCES agencies(id) ON DELETE CASCADE,
    travel_agency_id BIGINT NOT NULL REFERENCES agencies(id) ON DELETE CASCADE,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    contact_name VARCHAR(140) NOT NULL,
    contact_phone VARCHAR(60),
    contact_email VARCHAR(255),
    units INTEGER,
    desired_from DATE,
    desired_to DATE,
    note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_hotel_offer_resv_offer ON hotel_offer_reservations (offer_id);
CREATE INDEX IF NOT EXISTS idx_hotel_offer_resv_hotel_agency ON hotel_offer_reservations (hotel_agency_id, created_at);
CREATE INDEX IF NOT EXISTS idx_hotel_offer_resv_travel_agency ON hotel_offer_reservations (travel_agency_id, created_at);
CREATE INDEX IF NOT EXISTS idx_hotel_offer_resv_status ON hotel_offer_reservations (status);

