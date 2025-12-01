ALTER TABLE "user" ADD registered_at TIMESTAMP DEFAULT '2025-12-01 15:01:16.066'::timestamp without time zone NOT NULL;
ALTER TABLE "user" ADD last_login_at TIMESTAMP NULL;
