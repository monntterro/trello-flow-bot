BEGIN;

WITH users_without_settings AS (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn
                                FROM users
                                WHERE user_settings_id IS NULL),
     new_settings_raw AS (
         INSERT INTO user_settings (utc_offset)
             SELECT 0
             FROM users_without_settings
             RETURNING id),
     new_settings AS (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn
                      FROM new_settings_raw)
UPDATE users u
SET user_settings_id = ns.id
FROM users_without_settings uws
         JOIN new_settings ns ON uws.rn = ns.rn
WHERE u.id = uws.id;

COMMIT;