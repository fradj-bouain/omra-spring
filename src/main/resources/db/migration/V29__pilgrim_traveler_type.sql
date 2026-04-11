-- Type de voyage (motif) pour chaque voyageur — défaut historique : pèlerinage
ALTER TABLE pilgrims ADD COLUMN IF NOT EXISTS traveler_type VARCHAR(32) NOT NULL DEFAULT 'PILGRIM';
