ALTER TABLE assets DROP CONSTRAINT child_assets_parent_asset_id_fkey;

ALTER TABLE assets ADD CONSTRAINT assets_parent_asset_id_fkey
    FOREIGN KEY (parent_asset_id) REFERENCES assets(id);