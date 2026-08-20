-- Remove foreign key constraint first
ALTER TABLE users
DROP
CONSTRAINT IF EXISTS fk_users_role;

-- Remove role_id column from users table
ALTER TABLE users
DROP
COLUMN IF EXISTS role_id;