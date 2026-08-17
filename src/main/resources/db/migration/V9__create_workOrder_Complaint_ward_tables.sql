create table work_order(
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id UUID NOT NULL REFERENCES assets(id),
    assigned_to UUID NOT NULL REFERENCES inspection(id),

    priority varchar(20),
    status varchar(20),
    due_date timestamp,
    created_at timestamp,
    closed_at timestamp
);


create table complaint(
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    citizen_id UUID NOT NULL REFERENCES users(id),
    category varchar(200),
    description varchar(200),
    location geometry(Point , 4326) NOT NULL,
    photo_url varchar(255),
    status varchar(200),
    linked_work_order_id UUID REFERENCES work_order(id)

);

create table ward (

                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name varchar(200),
    boundary geometry(Polygon, 4326) NOT NULL,
    population int
);


