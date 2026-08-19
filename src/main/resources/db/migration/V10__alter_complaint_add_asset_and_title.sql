CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE complaint
DROP COLUMN IF EXISTS category;

ALTER TABLE complaint
    ADD COLUMN IF NOT EXISTS title VARCHAR(255);

ALTER TABLE complaint
    ADD COLUMN IF NOT EXISTS asset_id UUID;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_complaint_asset'
    ) THEN
ALTER TABLE complaint
    ADD CONSTRAINT fk_complaint_asset
        FOREIGN KEY (asset_id)
            REFERENCES assets(id);
END IF;
END $$;

ALTER TABLE complaint
DROP COLUMN IF EXISTS photo_url;

CREATE TABLE IF NOT EXISTS complaint_photo
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    complaint_id UUID NOT NULL,

    photo_url VARCHAR(500) NOT NULL,

    CONSTRAINT fk_complaint_photo_complaint
    FOREIGN KEY (complaint_id)
    REFERENCES complaint(id)
    ON DELETE CASCADE
    );

ALTER TABLE complaint
    ALTER COLUMN status SET DEFAULT 'SUBMITTED';