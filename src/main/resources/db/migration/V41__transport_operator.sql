-- Transport carriers (TRANSPORT agencies): vehicles, bookable offers, reservations from TRAVEL agencies.

CREATE TABLE IF NOT EXISTS transport_vehicles (
    id              BIGSERIAL PRIMARY KEY,
    agency_id       BIGINT       NOT NULL REFERENCES agencies (id) ON DELETE CASCADE,
    vehicle_type    VARCHAR(16)  NOT NULL,
    seat_count      INTEGER      NOT NULL,
    plate           VARCHAR(64),
    brand           VARCHAR(120),
    driver_name     VARCHAR(140),
    driver_phone    VARCHAR(60),
    driver_email    VARCHAR(255),
    address         TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ,
    deleted_at      TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_transport_vehicles_agency ON transport_vehicles (agency_id);
CREATE INDEX IF NOT EXISTS idx_transport_vehicles_agency_not_deleted
    ON transport_vehicles (agency_id) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS transport_offers (
    id              BIGSERIAL PRIMARY KEY,
    agency_id       BIGINT       NOT NULL REFERENCES agencies (id) ON DELETE CASCADE,
    vehicle_id      BIGINT       NOT NULL REFERENCES transport_vehicles (id) ON DELETE CASCADE,
    title           VARCHAR(220) NOT NULL,
    description     TEXT,
    image_url       VARCHAR(1024),
    status          VARCHAR(16)  NOT NULL DEFAULT 'DISABLED',
    pricing_unit    VARCHAR(16)  NOT NULL,
    price           NUMERIC(19, 4) NOT NULL,
    currency        VARCHAR(8)   NOT NULL,
    min_units       INTEGER,
    max_units       INTEGER,
    valid_from      DATE         NOT NULL,
    valid_to        DATE         NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ,
    deleted_at      TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_transport_offers_agency ON transport_offers (agency_id);
CREATE INDEX IF NOT EXISTS idx_transport_offers_vehicle ON transport_offers (vehicle_id);
CREATE INDEX IF NOT EXISTS idx_transport_offers_status ON transport_offers (status);
CREATE INDEX IF NOT EXISTS idx_transport_offers_agency_not_deleted
    ON transport_offers (agency_id) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS transport_offer_reservations (
    id                     BIGSERIAL PRIMARY KEY,
    offer_id               BIGINT       NOT NULL REFERENCES transport_offers (id) ON DELETE CASCADE,
    transport_agency_id    BIGINT       NOT NULL REFERENCES agencies (id) ON DELETE CASCADE,
    travel_agency_id       BIGINT       NOT NULL REFERENCES agencies (id) ON DELETE CASCADE,
    status                 VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    contact_name           VARCHAR(140) NOT NULL,
    contact_phone          VARCHAR(60),
    contact_email          VARCHAR(255),
    units                  INTEGER,
    desired_from           DATE,
    desired_to             DATE,
    note                   TEXT,
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_transport_offer_resv_offer ON transport_offer_reservations (offer_id);
CREATE INDEX IF NOT EXISTS idx_transport_offer_resv_transport_agency ON transport_offer_reservations (transport_agency_id, created_at);
CREATE INDEX IF NOT EXISTS idx_transport_offer_resv_travel_agency ON transport_offer_reservations (travel_agency_id, created_at);
CREATE INDEX IF NOT EXISTS idx_transport_offer_resv_status ON transport_offer_reservations (status);
