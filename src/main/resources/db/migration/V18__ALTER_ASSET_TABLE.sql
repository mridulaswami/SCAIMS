-- Rename table
ALTER TABLE child_assets
    RENAME TO assets;

-- Add column if it does not already exist
ALTER TABLE assets
    ADD COLUMN IF NOT EXISTS parent_asset_id UUID;

-- Remove invalid parent references
UPDATE assets
SET parent_asset_id = NULL
WHERE parent_asset_id IS NOT NULL
  AND parent_asset_id NOT IN (SELECT id
                              FROM assets);

-- Add self-referencing foreign key
ALTER TABLE assets
    ADD CONSTRAINT fk_assets_parent_asset
        FOREIGN KEY (parent_asset_id)
            REFERENCES assets (id);