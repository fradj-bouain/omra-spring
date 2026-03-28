-- Réparation : aligner la base sur l’entité sans reward_tier_id sur referral_campaigns.
-- À exécuter si V23 n’a pas tourné jusqu’au bout ou si la colonne est restée NOT NULL.

CREATE TABLE IF NOT EXISTS referral_campaign_slots (
    id BIGSERIAL PRIMARY KEY,
    campaign_id BIGINT NOT NULL REFERENCES referral_campaigns(id) ON DELETE CASCADE,
    rank_order INT NOT NULL CHECK (rank_order >= 1),
    reward_tier_id BIGINT NOT NULL REFERENCES referral_reward_tiers(id),
    CONSTRAINT uq_referral_campaign_slot_rank UNIQUE (campaign_id, rank_order)
);

CREATE INDEX IF NOT EXISTS idx_referral_campaign_slots_campaign ON referral_campaign_slots(campaign_id);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'referral_campaigns'
          AND column_name = 'reward_tier_id'
    ) THEN
        INSERT INTO referral_campaign_slots (campaign_id, rank_order, reward_tier_id)
        SELECT c.id, gs.rank, c.reward_tier_id
        FROM referral_campaigns c
        CROSS JOIN LATERAL generate_series(1, c.max_winners) AS gs(rank)
        WHERE c.reward_tier_id IS NOT NULL
          AND NOT EXISTS (SELECT 1 FROM referral_campaign_slots s WHERE s.campaign_id = c.id);
    END IF;
END $$;

ALTER TABLE referral_campaign_winners
    ADD COLUMN IF NOT EXISTS reward_tier_id BIGINT REFERENCES referral_reward_tiers(id);

UPDATE referral_campaign_winners w
SET reward_tier_id = s.reward_tier_id
FROM referral_campaign_slots s
WHERE w.reward_tier_id IS NULL
  AND s.campaign_id = w.campaign_id
  AND s.rank_order = w.rank_order;

ALTER TABLE referral_campaigns DROP COLUMN IF EXISTS reward_tier_id;
