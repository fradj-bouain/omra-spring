-- Align users.role check constraint with UserRole enum (include PILGRIM_COMPANION and all current values)
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN (
    'SUPER_ADMIN',
    'AGENCY_ADMIN',
    'AGENCY_AGENT',
    'PILGRIM_COMPANION',
    'PILGRIM'
));
