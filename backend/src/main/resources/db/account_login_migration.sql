SET NAMES utf8mb4;
USE smart_community;

ALTER TABLE `user`
  ADD COLUMN IF NOT EXISTS `account` VARCHAR(32) DEFAULT NULL COMMENT 'Login account',
  ADD COLUMN IF NOT EXISTS `password` VARCHAR(64) DEFAULT NULL COMMENT 'Login password',
  ADD COLUMN IF NOT EXISTS `must_change_password` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '1 means first login must change password',
  MODIFY COLUMN `openid` VARCHAR(64) NULL COMMENT 'Wechat openid (optional)';

ALTER TABLE `property`
  ADD COLUMN IF NOT EXISTS `property_code` VARCHAR(32) DEFAULT NULL COMMENT 'Property code for owner login account';

SET @idx_account_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'user'
    AND index_name = 'uk_account'
);
SET @idx_account_sql := IF(@idx_account_exists = 0, 'ALTER TABLE `user` ADD UNIQUE KEY `uk_account` (`account`)', 'SELECT 1');
PREPARE idx_account_stmt FROM @idx_account_sql;
EXECUTE idx_account_stmt;
DEALLOCATE PREPARE idx_account_stmt;

SET @idx_property_code_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'property'
    AND index_name = 'uk_property_code'
);
SET @idx_property_code_sql := IF(@idx_property_code_exists = 0, 'ALTER TABLE `property` ADD UNIQUE KEY `uk_property_code` (`property_code`)', 'SELECT 1');
PREPARE idx_property_code_stmt FROM @idx_property_code_sql;
EXECUTE idx_property_code_stmt;
DEALLOCATE PREPARE idx_property_code_stmt;

UPDATE `property`
SET `property_code` = CONCAT(
    'YZ',
    LPAD(CAST(COALESCE(NULLIF(REGEXP_SUBSTR(`building`, '[0-9]+'), ''), '0') AS UNSIGNED) % 100, 2, '0'),
    LPAD(CAST(COALESCE(NULLIF(REGEXP_SUBSTR(`unit`, '[0-9]+'), ''), '0') AS UNSIGNED) % 100, 2, '0'),
    LPAD(CAST(COALESCE(NULLIF(REGEXP_SUBSTR(`room`, '[0-9]+'), ''), '0') AS UNSIGNED) % 1000, 3, '0'),
    DATE_FORMAT(COALESCE(`create_time`, NOW()), '%y')
)
WHERE `property_code` IS NULL OR `property_code` = '';

UPDATE `user` u
JOIN `user_property` up ON up.`user_id` = u.`id`
  AND up.`is_deleted` = 0
  AND up.`is_primary` = 1
JOIN `property` p ON p.`id` = up.`property_id`
  AND p.`is_deleted` = 0
SET u.`account` = p.`property_code`,
    u.`password` = REVERSE(RIGHT(CONCAT(REGEXP_REPLACE(p.`property_code`, '[^0-9]', ''), '000000'), 6)),
    u.`must_change_password` = 0,
    u.`update_time` = NOW()
WHERE u.`role` = 1
  AND p.`property_code` IS NOT NULL
  AND p.`property_code` <> ''
  AND (u.`account` IS NULL OR u.`account` = '' OR u.`account` LIKE 'XQYZ%');

SET @admin_seed = FLOOR(RAND() * 1000000);
SET @worker_seed = FLOOR(RAND() * 1000000);

UPDATE `user`
SET `account` = CONCAT('WTGL', LPAD(MOD(`id` * 3251 + @admin_seed, 1000000), 6, '0')),
    `password` = REVERSE(RIGHT(CONCAT('000000', MOD(`id` * 3251 + @admin_seed, 1000000)), 6)),
    `update_time` = NOW()
WHERE `role` = 2
  AND (`account` IS NULL OR `account` = '' OR `account` NOT REGEXP '^WTGL[0-9]{6}$' OR `password` IS NULL OR `password` = '');

UPDATE `user`
SET `account` = CONCAT('JZWX', LPAD(MOD(`id` * 9473 + @worker_seed, 1000000), 6, '0')),
    `password` = REVERSE(RIGHT(CONCAT('000000', MOD(`id` * 9473 + @worker_seed, 1000000)), 6)),
    `update_time` = NOW()
WHERE `role` = 3
  AND (`account` IS NULL OR `account` = '' OR `account` NOT REGEXP '^JZWX[0-9]{6}$' OR `password` IS NULL OR `password` = '');

SELECT `id`, `role`, `account`, `password`, `must_change_password`, `nickname`
FROM `user`
WHERE `role` IN (1, 2, 3)
ORDER BY `role`, `id`;
