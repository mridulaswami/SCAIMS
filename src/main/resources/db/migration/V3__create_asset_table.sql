CREATE EXTENSION IF NOT EXISTS postgis;

create table asset_category (

                                id UUID primary key default gen_random_uuid(),
                                name varchar(255) not null,
                                icon_key VARCHAR(255),
                                default_layer_color VARCHAR(255)
);


create table assets(

                       id UUID primary key default gen_random_uuid(),
    name VARCHAR(255) not null ,
    category_id UUID NOT NULL references asset_category(id),
    geometry geometry(Geometry , 4326) not null,
    status varchar(50),
    condition varchar(100),
    ward varchar(100),
    installed_date timestamp ,
    last_inspected_date timestamp
);

CREATE INDEX idx_assets_geometry ON assets USING GIST (geometry);