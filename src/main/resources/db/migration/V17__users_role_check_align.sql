-- Fix: some DBs still have an older users_role_check without PILGRIM_COMPANION (insert then fails).
-- Align with com.omra.platform.entity.enums.UserRole
ALTER TABLE public.users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE public.users ADD CONSTRAINT users_role_check CHECK (role IN (
    'SUPER_ADMIN',
    'AGENCY_ADMIN',
    'AGENCY_AGENT',
    'PILGRIM_COMPANION',
    'PILGRIM'
));
