ALTER TABLE "user" ALTER COLUMN registered_at TYPE TIMESTAMP, ALTER COLUMN registered_at SET DEFAULT '2025-12-01 19:04:11.035'::timestamp without time zone;
ALTER TABLE test ADD questions_count INT DEFAULT 0 NOT NULL;
