CREATE TABLE IF NOT EXISTS survey (
    id uuid PRIMARY KEY,
    "name" TEXT NOT NULL,
    description TEXT NOT NULL,
    duration_mins TEXT NOT NULL,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    "position" INT NOT NULL
);

CREATE TABLE IF NOT EXISTS survey_session (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    survey_id uuid NOT NULL,
    answers JSONB NOT NULL,
    status VARCHAR(11) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    closed_at TIMESTAMP NULL,
    CONSTRAINT fk_survey_session_user_id__id
        FOREIGN KEY (user_id) REFERENCES "user"(id)
        ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_survey_session_survey_id__id
        FOREIGN KEY (survey_id) REFERENCES survey(id)
        ON DELETE CASCADE ON UPDATE RESTRICT
);

ALTER TABLE question ALTER COLUMN test_id DROP NOT NULL;
ALTER TABLE question ADD COLUMN survey_id uuid NULL;
ALTER TABLE question ADD CONSTRAINT fk_question_survey_id__id
    FOREIGN KEY (survey_id) REFERENCES survey(id)
    ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE question ADD CONSTRAINT question_parent_xor
    CHECK ((test_id IS NULL) <> (survey_id IS NULL));
