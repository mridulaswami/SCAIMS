ALTER TABLE inspection
    DROP COLUMN IF EXISTS condition_rating,
    DROP COLUMN IF EXISTS photo_url;

alter table assets add column assigned_inspector_id UUID ;

CREATE TABLE inspection_photos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inspection_id UUID NOT NULL REFERENCES inspection(id),
    photo_url VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP


);