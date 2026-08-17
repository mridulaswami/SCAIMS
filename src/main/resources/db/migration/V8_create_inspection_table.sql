create table inspection(

                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           asset_id UUID NOT NULL REFERENCES assets(id),
                           inspector_user_id UUID NOT NULL REFERENCES users(id),
                           condition_rating varchar(255),
                           notes varchar(255),
                           photo_url varchar(255),
                           inspected_at timestamp,
                           geo_tag geometry(Point, 4326) NOT NULL

);