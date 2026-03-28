-- Parrainage pèlerin → pèlerin : parrain, points, paliers de cadeaux
ALTER TABLE pilgrims ADD COLUMN IF NOT EXISTS sponsor_type VARCHAR(16);
ALTER TABLE pilgrims ADD COLUMN IF NOT EXISTS sponsor_label VARCHAR(255);
ALTER TABLE pilgrims ADD COLUMN IF NOT EXISTS referrer_pilgrim_id BIGINT REFERENCES pilgrims(id) ON DELETE SET NULL;
ALTER TABLE pilgrims ADD COLUMN IF NOT EXISTS referral_points INTEGER NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_pilgrim_referrer_pilgrim ON pilgrims(referrer_pilgrim_id);

CREATE TABLE IF NOT EXISTS pilgrim_sponsor_events (
    id BIGSERIAL PRIMARY KEY,
    agency_id BIGINT NOT NULL REFERENCES agencies(id) ON DELETE CASCADE,
    referrer_pilgrim_id BIGINT NOT NULL REFERENCES pilgrims(id) ON DELETE CASCADE,
    referred_pilgrim_id BIGINT NOT NULL REFERENCES pilgrims(id) ON DELETE CASCADE,
    points_awarded INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_sponsor_event_referred UNIQUE (referred_pilgrim_id)
);

CREATE INDEX IF NOT EXISTS idx_sponsor_event_referrer ON pilgrim_sponsor_events(referrer_pilgrim_id);
CREATE INDEX IF NOT EXISTS idx_sponsor_event_agency ON pilgrim_sponsor_events(agency_id);

CREATE TABLE IF NOT EXISTS referral_reward_tiers (
    id BIGSERIAL PRIMARY KEY,
    agency_id BIGINT NOT NULL REFERENCES agencies(id) ON DELETE CASCADE,
    points_threshold INT NOT NULL,
    gift_title VARCHAR(255) NOT NULL,
    gift_description TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT uq_tier_agency_points UNIQUE (agency_id, points_threshold)
);

CREATE INDEX IF NOT EXISTS idx_reward_tier_agency ON referral_reward_tiers(agency_id) WHERE deleted_at IS NULL;
