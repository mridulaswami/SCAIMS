Alter table inspection add column priority varchar(200);
Alter table inspection add column status varchar(200);
Alter table inspection add column work_report varchar(200);
Alter table inspection add column rejection_reason varchar(200);
Alter table inspection add column created_by UUID ;

alter table inspection drop column geo_tag;

CREATE TABLE inspection_status_Audits (
                                                 id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                 previous_status     VARCHAR(20) NOT NULL,
                                                 current_status      VARCHAR(20) NOT NULL,
                                                 change_at           TIMESTAMP NOT NULL,
                                                 changed_by          UUID NOT NULL,
                                                 inspection_id       UUID NOT NULL,
                                                 created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                 updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                                 CONSTRAINT fk_isa_changed_by
                                                     FOREIGN KEY (changed_by) REFERENCES users(id),

                                                 CONSTRAINT fk_isa_inspection
                                                     FOREIGN KEY (inspection_id) REFERENCES inspection(id)
);