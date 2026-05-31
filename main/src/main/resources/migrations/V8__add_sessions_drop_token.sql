CREATE TABLE IF NOT EXISTS "session" (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    user_agent TEXT NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    last_login_at TIMESTAMP NOT NULL
);

ALTER TABLE "session"
    ADD CONSTRAINT fk_session_user_id__id
    FOREIGN KEY (user_id) REFERENCES "user"(id)
    ON DELETE CASCADE ON UPDATE RESTRICT;

DROP TABLE IF EXISTS token;
