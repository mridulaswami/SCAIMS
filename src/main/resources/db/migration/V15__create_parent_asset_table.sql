

CREATE TABLE parent_assets (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               name VARCHAR(255) NOT NULL,
                               category_id UUID NOT NULL REFERENCES asset_category(id),
                               geometry geometry(Polygon, 4326) NOT NULL,
                               status VARCHAR(50),
                               condition VARCHAR(100),
                               ward VARCHAR(100),
                               installed_date TIMESTAMP,
                               last_inspected_date TIMESTAMP
);

CREATE INDEX idx_parent_assets_geometry ON parent_assets USING GIST (geometry);


ALTER TABLE assets RENAME TO child_assets;

ALTER TABLE child_assets
    ADD COLUMN parent_asset_id UUID REFERENCES parent_assets(id);

CREATE INDEX idx_child_assets_parent ON child_assets (parent_asset_id);

