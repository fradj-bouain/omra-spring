-- Catalogue d'offres (créé / géré par super-admin)
CREATE TABLE subscription_plans (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(14, 2) NOT NULL DEFAULT 0,
    currency VARCHAR(8) NOT NULL DEFAULT 'MAD',
    billing_period VARCHAR(24) NOT NULL,
    default_duration_days INT,
    max_users INT,
    features TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subscription_plans_active ON subscription_plans (active, sort_order);

-- Historique / période en cours par agence
CREATE TABLE agency_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    agency_id BIGINT NOT NULL REFERENCES agencies (id) ON DELETE CASCADE,
    plan_id BIGINT NOT NULL REFERENCES subscription_plans (id) ON DELETE RESTRICT,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    paid_at TIMESTAMPTZ,
    payment_reference VARCHAR(255),
    amount_paid NUMERIC(14, 2),
    currency VARCHAR(8),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_agency_subscriptions_agency ON agency_subscriptions (agency_id);
CREATE INDEX idx_agency_subscriptions_agency_status ON agency_subscriptions (agency_id, status);
CREATE INDEX idx_agency_subscriptions_period ON agency_subscriptions (status, period_end);

-- Plan technique pour données existantes
INSERT INTO subscription_plans (code, name, description, price, currency, billing_period, default_duration_days, active, sort_order)
VALUES (
        'LEGACY',
        'Forfait historique',
        'Abonnement initial après migration — considéré comme payé pour conserver l''accès existant.',
        0,
        'MAD',
        'ONE_TIME',
        NULL,
        TRUE,
        0
    );

INSERT INTO subscription_plans (code, name, description, price, currency, billing_period, default_duration_days, active, sort_order)
VALUES
    ('STANDARD', 'Standard', 'Offre standard agence — facturation annuelle suggérée.', 12000, 'MAD', 'YEARLY', 365, TRUE, 10),
    ('PREMIUM', 'Premium', 'Offre étendue (plus d''utilisateurs / options).', 24000, 'MAD', 'YEARLY', 365, TRUE, 20);

-- Abonnement de continuité pour chaque agence (évite de bloquer les comptes existants)
INSERT INTO agency_subscriptions (
        agency_id,
        plan_id,
        period_start,
        period_end,
        status,
        paid_at,
        amount_paid,
        currency
    )
SELECT a.id,
    (SELECT id FROM subscription_plans WHERE code = 'LEGACY' LIMIT 1),
    COALESCE(a.subscription_start_date, CURRENT_DATE),
    GREATEST(
        COALESCE(a.subscription_end_date, CURRENT_DATE + INTERVAL '365 days')::date,
        CURRENT_DATE
    ),
    'ACTIVE',
    NOW(),
    0,
    'MAD'
FROM agencies a;
