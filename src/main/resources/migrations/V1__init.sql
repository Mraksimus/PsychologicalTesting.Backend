CREATE TABLE IF NOT EXISTS "user" (id uuid PRIMARY KEY, email TEXT NOT NULL, "password" TEXT NOT NULL);
CREATE UNIQUE INDEX unique_email_lowercase ON "user" (LOWER("user".email));
CREATE TABLE IF NOT EXISTS token (user_id uuid NOT NULL, "value" VARCHAR(64) NOT NULL, created_at TIMESTAMP NOT NULL, expires_at TIMESTAMP NOT NULL, CONSTRAINT fk_token_user_id__id FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE ON UPDATE RESTRICT);
