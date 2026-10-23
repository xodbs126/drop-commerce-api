ALTER TABLE app_user
    ADD COLUMN username VARCHAR(50) NOT NULL,
    ADD COLUMN password VARCHAR(255) NOT NULL,
    ADD CONSTRAINT uk_app_user_username UNIQUE (username);