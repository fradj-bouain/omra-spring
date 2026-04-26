-- Hotel-operator model: properties managed by HOTEL-kind agencies and bookable offers.

CREATE TABLE IF NOT EXISTS hotel_properties (
    id              BIGSERIAL PRIMARY KEY,
    agency_id       BIGINT       NOT NULL,
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    city            VARCHAR(120),
    country         VARCHAR(2),
    address         TEXT,
    image_url       VARCHAR(1024),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_hotel_properties_agency ON hotel_properties (agency_id);

CREATE TABLE IF NOT EXISTS hotel_offers (
    id              BIGSERIAL PRIMARY KEY,
    agency_id       BIGINT       NOT NULL,
    property_id     BIGINT       NOT NULL REFERENCES hotel_properties (id) ON DELETE CASCADE,
    title           VARCHAR(220) NOT NULL,
    description     TEXT,
    image_url       VARCHAR(1024),
    pricing_unit    VARCHAR(16)  NOT NULL,
    price           NUMERIC(19, 4) NOT NULL,
    currency        VARCHAR(8)   NOT NULL,
    min_units       INTEGER,
    max_units       INTEGER,
    valid_from      DATE         NOT NULL,
    valid_to        DATE         NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_hotel_offers_agency ON hotel_offers (agency_id);
CREATE INDEX IF NOT EXISTS idx_hotel_offers_property ON hotel_offers (property_id);
