CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX idx_workers_email_trgm ON workers USING gin (email gin_trgm_ops);
