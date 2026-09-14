ALTER TABLE "user" ALTER COLUMN registered_at TYPE TIMESTAMP, ALTER COLUMN registered_at SET DEFAULT '2026-04-24 01:40:16.569'::timestamp without time zone;
ALTER TABLE testing_session ADD aswers JSONB NOT NULL;
ALTER TABLE testing_session DROP COLUMN question_responses;
