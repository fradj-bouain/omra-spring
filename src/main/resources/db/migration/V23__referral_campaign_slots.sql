-- Un palier / cadeau par rang (1er, 2e, 3e…) pour chaque campagne

CREATE TABLE referral_campaign_slots (
    id BIGSERIAL PRIMARY KEY,
    campaign_id BIGINT NOT NULL REFERENCES referral_campaigns(id) ON DELETE CASCADE,
    rank_order INT NOT NULL CHECK (rank_order >= 1),
    reward_tier_id BIGINT NOT NULL REFERENCES referral_reward_tiers(id),
    CONSTRAINT uq_referral_campaign_slot_rank UNIQUE (campaign_id, rank_order)
);

CREATE INDEX idx_referral_campaign_slots_campaign ON referral_campaign_slots(campaign_id);

INSERT INTO referral_campaign_slots (campaign_id, rank_order, reward_tier_id)
SELECT c.id, gs.rank, c.reward_tier_id
FROM referral_campaigns c
CROSS JOIN LATERAL generate_series(1, c.max_winners) AS gs(rank);

ALTER TABLE referral_campaign_winners
    ADD COLUMN reward_tier_id BIGINT REFERENCES referral_reward_tiers(id);

UPDATE referral_campaign_winners w
SET reward_tier_id = s.reward_tier_id
FROM referral_campaign_slots s
WHERE s.campaign_id = w.campaign_id
  AND s.rank_order = w.rank_order;

ALTER TABLE referral_campaigns DROP COLUMN reward_tier_id;
