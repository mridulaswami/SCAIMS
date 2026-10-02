
alter table inspection drop column condition_rating;
alter table inspection drop column photo_url;




CREATE TABLE inspection_photos (
                                          id uuid DEFAULT gen_random_uuid() NOT NULL,
                                          inspection_id uuid NOT NULL,
                                          photo_url varchar(500) NOT NULL,
                                          created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                          updated_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                          CONSTRAINT inspection_photos_created_at_not_null NOT NULL created_at,
                                          CONSTRAINT inspection_photos_id_not_null NOT NULL id,
                                          CONSTRAINT inspection_photos_inspection_id_not_null NOT NULL inspection_id,
                                          CONSTRAINT inspection_photos_photo_url_not_null NOT NULL photo_url,
                                          CONSTRAINT inspection_photos_pkey PRIMARY KEY (id),
                                          CONSTRAINT inspection_photos_updated_at_not_null NOT NULL updated_at
);


ALTER TABLE inspection_photos ADD CONSTRAINT inspection_photos_inspection_id_fkey FOREIGN KEY (inspection_id) REFERENCES inspection(id);