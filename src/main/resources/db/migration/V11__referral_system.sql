-- Parrainage: code unique par utilisateur et lien referrer → referred
ALTER TABLE users ADD COLUMN IF NOT EXISTS referral_code VARCHAR(16) UNIQUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS referred_by_id BIGINT REFERENCES users(id) ON DELETE SET NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_referral_code ON users(referral_code) WHERE referral_code IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_user_referred_by ON users(referred_by_id);

-- Table des parrainages (referrer → referred, statut, récompense)
CREATE TABLE IF NOT EXISTS referrals (
    id BIGSERIAL PRIMARY KEY,
    referrer_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    referred_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    reward_given BOOLEAN NOT NULL DEFAULT FALSE,
    reward_granted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(referred_id)
);
CREATE INDEX IF NOT EXISTS idx_referral_referrer ON referrals(referrer_id);
CREATE INDEX IF NOT EXISTS idx_referral_referred ON referrals(referred_id);
CREATE INDEX IF NOT EXISTS idx_referral_status ON referrals(status);

-- Backfill: generate referral codes for existing users (optional, run in app or separate script)
-- UPDATE users SET referral_code = ... WHERE referral_code IS NULL;
