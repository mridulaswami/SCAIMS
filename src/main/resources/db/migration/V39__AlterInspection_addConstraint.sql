alter table inspection add CONSTRAINT fk_isa_created_by
    FOREIGN KEY (created_by) REFERENCES users(id);