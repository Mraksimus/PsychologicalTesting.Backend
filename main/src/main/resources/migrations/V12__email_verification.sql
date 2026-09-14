ALTER TABLE "user" ADD COLUMN email_verified_at TIMESTAMP NULL;

CREATE TABLE IF NOT EXISTS email_verification_token (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    token TEXT NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    CONSTRAINT fk_email_verification_token_user_id__id
        FOREIGN KEY (user_id) REFERENCES "user"(id)
        ON DELETE CASCADE ON UPDATE RESTRICT
);

CREATE INDEX IF NOT EXISTS ix_email_verification_token_user_id
    ON email_verification_token(user_id);
