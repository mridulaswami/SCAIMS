-- V28__change_role_id_to_integer.sql

-- 1. Temporarily disable FK checks
SET
session_replication_role = 'replica';


-- 2. Remove old UUID default from roles.id
ALTER TABLE roles
    ALTER COLUMN id DROP DEFAULT;


-- 3. Drop FK from user_roles
ALTER TABLE user_roles
DROP
CONSTRAINT IF EXISTS fk_user_roles_role;


-- 4. Since this is development data, clear existing data
TRUNCATE TABLE user_roles, roles RESTART IDENTITY;


-- 5. Change user_roles.role_id UUID -> INTEGER
ALTER TABLE user_roles
ALTER
COLUMN role_id TYPE INTEGER
USING NULL;


-- 6. Change roles.id UUID -> INTEGER
ALTER TABLE roles
ALTER
COLUMN id TYPE INTEGER
USING NULL;


-- 7. Create integer sequence
DROP SEQUENCE IF EXISTS roles_id_seq;

CREATE SEQUENCE roles_id_seq
    START WITH 1
    INCREMENT BY 1;


-- 8. Set sequence as default
ALTER TABLE roles
    ALTER COLUMN id SET DEFAULT nextval('roles_id_seq');


-- 9. Make sequence owned by roles.id
ALTER SEQUENCE roles_id_seq
    OWNED BY roles.id;


-- 10. Recreate FK
ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id)
            REFERENCES roles (id);


-- 11. Insert default roles
INSERT INTO roles
    (id, role_name, description, created_at, updated_at)
VALUES (1, 'ADMIN', 'System Administrator', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (2, 'CITIZEN', 'Citizen User', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (3, 'FIELD_ENGINEER', 'Field Engineer', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- 12. Set sequence so next ID = 4
SELECT setval('roles_id_seq', 3, true);


-- 13. Enable FK checks
SET
session_replication_role = 'origin';