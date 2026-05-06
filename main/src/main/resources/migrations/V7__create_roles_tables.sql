CREATE TABLE IF NOT EXISTS role (
    id uuid PRIMARY KEY,
    "name" VARCHAR(32) NOT NULL,
    color CHAR(7) NULL,
    permissions JSONB NOT NULL
);

CREATE UNIQUE INDEX role__name_lowercase_unique ON role (LOWER(role."name"));

ALTER TABLE "user"
    ADD COLUMN role_id uuid NULL,
    ADD CONSTRAINT fk_user_role_id__id FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE SET NULL ON UPDATE RESTRICT;
