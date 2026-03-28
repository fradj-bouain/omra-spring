-- Jeux / campagnes de parrainage : fenêtre temporelle, palier lié, N premiers gagnants

CREATE TABLE referral_campaigns (
    id BIGSERIAL PRIMARY KEY,
    agency_id BIGINT NOT NULL REFERENCES agencies(id) ON DELETE CASCADE,
    title VARCHAR(255),
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    reward_tier_id BIGINT NOT NULL REFERENCES referral_reward_tiers(id),
    max_winners INT NOT NULL CHECK (max_winners >= 1 AND max_winners <= 500),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closed_at TIMESTAMPTZ,
    CONSTRAINT chk_referral_campaign_dates CHECK (ends_at > starts_at)
);

CREATE UNIQUE INDEX uq_referral_campaign_agency_active ON referral_campaigns (agency_id) WHERE (status = 'ACTIVE');

CREATE INDEX idx_referral_campaign_agency_created ON referral_campaigns(agency_id, created_at DESC);

CREATE TABLE referral_campaign_winners (
    id BIGSERIAL PRIMARY KEY,
    campaign_id BIGINT NOT NULL REFERENCES referral_campaigns(id) ON DELETE CASCADE,
    pilgrim_id BIGINT NOT NULL REFERENCES pilgrims(id) ON DELETE CASCADE,
    rank_order INT NOT NULL CHECK (rank_order >= 1),
    won_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    points_at_win INT NOT NULL,
    CONSTRAINT uq_campaign_winner_pilgrim UNIQUE (campaign_id, pilgrim_id),
    CONSTRAINT uq_campaign_winner_rank UNIQUE (campaign_id, rank_order)
);

CREATE INDEX idx_campaign_winners_campaign ON referral_campaign_winners(campaign_id);
