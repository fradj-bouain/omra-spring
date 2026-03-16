-- Full audit log schema: request/response and metadata
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS user_email VARCHAR(255);
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS action_type VARCHAR(64);
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS entity_type VARCHAR(128);
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS referenced_entity_id BIGINT;
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS api_endpoint VARCHAR(512);
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS http_method VARCHAR(16);
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS request_data TEXT;
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS response_data TEXT;
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS status_code INT;
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS ip_address VARCHAR(64);
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_audit_created_at ON audit_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_action_type ON audit_logs(action_type);
CREATE INDEX IF NOT EXISTS idx_audit_entity_type_id ON audit_logs(entity_type, referenced_entity_id);
