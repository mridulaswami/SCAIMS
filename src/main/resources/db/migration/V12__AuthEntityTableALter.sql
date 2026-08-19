ALTER TABLE auths
ALTER COLUMN access_token_expire_at TYPE TIMESTAMP
        USING access_token_expire_at::TIMESTAMP,
    ALTER COLUMN refresh_token_expire_at TYPE TIMESTAMP
        USING refresh_token_expire_at::TIMESTAMP,
    ALTER COLUMN access_token_expire_at SET NOT NULL,
    ALTER COLUMN refresh_token_expire_at SET NOT NULL;