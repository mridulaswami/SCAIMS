insert into assets( name, category_id, geometry , status, condition, ward)
values ('Test Streetlight',
        (Select id from asset_category where name ='StreetLight'),
        ST_GeomFromText('Point(77.7890 28.1350)', 4326),
        'ACTIVE', 'DAMAGED' , 'CP'
        ),
('Test Drainage' ,
(select id from asset_category where name='DrainageLine'),
 ST_GeomFromText('LINESTRING(77.2280 28.1234 , 77.2300 28.1300 , 77.2350 28.1400)' , 4326),
 'ACTIVE' ,'GOOD' ,'Rajiv Chowk'
),
('Test Building',
 (select id from asset_category where name='Building'),
 ST_GeomFromText('POLYGON((77.1100 28.1100 , 77.1200 28.1200 , 77.1300 28.1300 , 77.1400 28.1400 , 77.1100 28.1100))',4326),
 'ACTIVE' ,'DAMAGED' , 'Hanuman Gali'
 )