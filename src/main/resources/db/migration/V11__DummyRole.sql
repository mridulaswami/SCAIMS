INSERT INTO roles (
    role_name,
    description,
    created_at,
    updated_at
)
VALUES
    ('ADMIN', 'Admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FIELD_ENGINEER', 'Field Engineer', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('CITIZEN', 'Citizen', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);