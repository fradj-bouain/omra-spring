-- Max active sub-agencies allowed for the main agency while this plan applies (NULL = unlimited).
ALTER TABLE subscription_plans ADD COLUMN IF NOT EXISTS max_sub_agencies INT;

UPDATE subscription_plans SET max_sub_agencies = NULL WHERE code = 'LEGACY';
UPDATE subscription_plans SET max_sub_agencies = 3 WHERE code = 'STANDARD';
UPDATE subscription_plans SET max_sub_agencies = 10 WHERE code = 'PREMIUM';
