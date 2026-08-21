-- =========================================================
-- 1. Update existing work_order table
-- =========================================================

-- Remove old relationships
ALTER TABLE work_order
DROP
COLUMN IF EXISTS asset_id;

ALTER TABLE work_order
DROP
COLUMN IF EXISTS assigned_to;


-- Add complaint relationship
ALTER TABLE work_order
    ADD COLUMN IF NOT EXISTS complaint_id UUID;

ALTER TABLE work_order
    ADD CONSTRAINT fk_work_order_complaint
        FOREIGN KEY (complaint_id)
            REFERENCES complaint (id);


-- Add inspector/user relationship
ALTER TABLE work_order
    ADD COLUMN IF NOT EXISTS user_id UUID;

ALTER TABLE work_order
    ADD CONSTRAINT fk_work_order_user
        FOREIGN KEY (user_id)
            REFERENCES users (id);


-- Rename/replace priority if required
-- Existing priority column can remain VARCHAR because
-- Java enum with @Enumerated(EnumType.STRING) stores:
-- LOW, MEDIUM, HIGH

ALTER TABLE work_order
ALTER
COLUMN priority TYPE VARCHAR(20);


-- Add work report / description
ALTER TABLE work_order
    ADD COLUMN IF NOT EXISTS description TEXT;


-- =========================================================
-- 2. Create work_order_photo table
-- =========================================================

CREATE TABLE IF NOT EXISTS work_order_photo
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),

    work_order_id UUID NOT NULL,

    photo_url VARCHAR
(
    500
) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_work_order_photo_work_order
    FOREIGN KEY
(
    work_order_id
)
    REFERENCES work_order
(
    id
)
    ON DELETE CASCADE
    );


-- =========================================================
-- 3. Indexes
-- =========================================================

CREATE INDEX IF NOT EXISTS idx_work_order_complaint_id
    ON work_order(complaint_id);

CREATE INDEX IF NOT EXISTS idx_work_order_user_id
    ON work_order(user_id);

CREATE INDEX IF NOT EXISTS idx_work_order_photo_work_order_id
    ON work_order_photo(work_order_id);