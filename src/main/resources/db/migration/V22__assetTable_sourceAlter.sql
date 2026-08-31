
ALTER TABLE assets ADD COLUMN source_type VARCHAR(20);
ALTER TABLE assets ADD COLUMN source_id int;

ALTER TABLE assets ADD CONSTRAINT uq_assets_osm_source UNIQUE (source_type, source_id);