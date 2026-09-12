CREATE TABLE IF NOT EXISTS category (
    id uuid PRIMARY KEY,
    "name" TEXT NOT NULL UNIQUE,
    color TEXT NOT NULL,
    icon TEXT NOT NULL,
    "position" INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

ALTER TABLE test ADD COLUMN category_id uuid NULL;
ALTER TABLE test ADD CONSTRAINT fk_test_category_id__id
    FOREIGN KEY (category_id) REFERENCES category(id)
    ON DELETE SET NULL ON UPDATE RESTRICT;

ALTER TABLE survey ADD COLUMN category_id uuid NULL;
ALTER TABLE survey ADD CONSTRAINT fk_survey_category_id__id
    FOREIGN KEY (category_id) REFERENCES category(id)
    ON DELETE SET NULL ON UPDATE RESTRICT;

INSERT INTO category (id, "name", color, icon, "position", created_at, updated_at) VALUES
    (gen_random_uuid(), 'Личность',   '#667eea', '🧠', 0, NOW(), NOW()),
    (gen_random_uuid(), 'Эмоции',     '#f5576c', '❤️', 1, NOW(), NOW()),
    (gen_random_uuid(), 'Интеллект',  '#4facfe', '💡', 2, NOW(), NOW()),
    (gen_random_uuid(), 'Карьера',    '#fa709a', '💼', 3, NOW(), NOW()),
    (gen_random_uuid(), 'Отношения',  '#30cfd0', '💑', 4, NOW(), NOW()),
    (gen_random_uuid(), 'Развитие',   '#a8edea', '📈', 5, NOW(), NOW()),
    (gen_random_uuid(), 'Другое',     '#d299c2', '📊', 6, NOW(), NOW());
