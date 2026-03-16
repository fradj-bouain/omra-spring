-- Paiement partiel : première échéance, période, nombre d'échéances
ALTER TABLE payments ADD COLUMN IF NOT EXISTS first_due_date DATE;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS due_period_days INT;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS number_of_installments INT;

-- Échéances générées pour les paiements partiels
CREATE TABLE IF NOT EXISTS payment_due (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    due_date DATE NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    sequence_order INT NOT NULL DEFAULT 1
);
CREATE INDEX IF NOT EXISTS idx_payment_due_payment_id ON payment_due(payment_id);
