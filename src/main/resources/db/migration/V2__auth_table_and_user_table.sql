CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                       user_name VARCHAR(255) NOT NULL UNIQUE,
                       name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,

                       role_id UUID NOT NULL,

                       status BOOLEAN NOT NULL DEFAULT TRUE,

                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_users_role
                           FOREIGN KEY (role_id)
                               REFERENCES roles (id)
);

CREATE TABLE auths (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                       access_token TEXT NOT NULL,
                       refresh_token TEXT NOT NULL,

                       access_token_expire_at DATE NOT NULL,
                       refresh_token_expire_at DATE NOT NULL,

                       user_id UUID NOT NULL,

                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_auths_user
                           FOREIGN KEY (user_id)
                               REFERENCES users (id)
);