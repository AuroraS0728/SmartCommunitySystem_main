SET NAMES utf8mb4;
USE smart_community;

ALTER TABLE `user`
  ADD COLUMN IF NOT EXISTS `account` VARCHAR(32) DEFAULT NULL COMMENT 'Login account',
  ADD COLUMN IF NOT EXISTS `password` VARCHAR(64) DEFAULT NULL COMMENT 'Login password',
  MODIFY COLUMN `openid` VARCHAR(64) NULL COMMENT 'Wechat openid (optional)';

SET @idx_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'user'
    AND index_name = 'uk_account'
);
SET @idx_sql := IF(@idx_exists = 0, 'ALTER TABLE `user` ADD UNIQUE KEY `uk_account` (`account`)', 'SELECT 1');
PREPARE idx_stmt FROM @idx_sql;
EXECUTE idx_stmt;
DEALLOCATE PREPARE idx_stmt;

SET @owner_seed = FLOOR(RAND() * 1000000);
SET @admin_seed = FLOOR(RAND() * 1000000);
SET @worker_seed = FLOOR(RAND() * 1000000);

UPDATE `user`
SET `account` = CONCAT('XQYZ', LPAD(MOD(`id` * 7193 + @owner_seed, 1000000), 6, '0')),
    `password` = REVERSE(LPAD(MOD(`id` * 7193 + @owner_seed, 1000000), 6, '0'))
WHERE `role` = 1
  AND (
    `account` IS NULL
    OR `account` = ''
    OR `account` NOT REGEXP '^XQYZ[0-9]{6}$'
    OR `password` IS NULL
    OR `password` <> REVERSE(RIGHT(`account`, 6))
  );

UPDATE `user`
SET `account` = CONCAT('WTGL', LPAD(MOD(`id` * 3251 + @admin_seed, 1000000), 6, '0')),
    `password` = REVERSE(LPAD(MOD(`id` * 3251 + @admin_seed, 1000000), 6, '0'))
WHERE `role` = 2
  AND (
    `account` IS NULL
    OR `account` = ''
    OR `account` NOT REGEXP '^WTGL[0-9]{6}$'
    OR `password` IS NULL
    OR `password` <> REVERSE(RIGHT(`account`, 6))
  );

UPDATE `user`
SET `account` = CONCAT('JZWX', LPAD(MOD(`id` * 9473 + @worker_seed, 1000000), 6, '0')),
    `password` = REVERSE(LPAD(MOD(`id` * 9473 + @worker_seed, 1000000), 6, '0'))
WHERE `role` = 3
  AND (
    `account` IS NULL
    OR `account` = ''
    OR `account` NOT REGEXP '^JZWX[0-9]{6}$'
    OR `password` IS NULL
    OR `password` <> REVERSE(RIGHT(`account`, 6))
  );

SELECT `id`, `role`, `account`, `password`, `nickname`
FROM `user`
WHERE `role` IN (1, 2, 3)
ORDER BY `role`, `id`;
