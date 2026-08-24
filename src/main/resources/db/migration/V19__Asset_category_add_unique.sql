ALTER TABLE asset_category
    ADD CONSTRAINT uk_asset_category_name UNIQUE (name);