
-- =========================================================
-- V26: Add created_at and updated_at to complaint table
-- =========================================================

ALTER TABLE complaint
    ADD COLUMN created_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE complaint
    ADD COLUMN updated_at TIMESTAMP WITHOUT TIME ZONE;

