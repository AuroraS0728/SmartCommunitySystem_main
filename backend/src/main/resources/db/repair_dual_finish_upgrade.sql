-- Repair order dual-confirmation upgrade
-- Execute this script on existing databases.

SET @db := DATABASE();

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db
        AND TABLE_NAME = 'repair_order'
        AND COLUMN_NAME = 'owner_finish_confirmed'
    ),
    'SELECT ''owner_finish_confirmed already exists''',
    'ALTER TABLE `repair_order` ADD COLUMN `owner_finish_confirmed` TINYINT(1) NOT NULL DEFAULT 0 AFTER `remark`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db
        AND TABLE_NAME = 'repair_order'
        AND COLUMN_NAME = 'owner_finish_time'
    ),
    'SELECT ''owner_finish_time already exists''',
    'ALTER TABLE `repair_order` ADD COLUMN `owner_finish_time` DATETIME DEFAULT NULL AFTER `owner_finish_confirmed`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db
        AND TABLE_NAME = 'repair_order'
        AND COLUMN_NAME = 'worker_finish_confirmed'
    ),
    'SELECT ''worker_finish_confirmed already exists''',
    'ALTER TABLE `repair_order` ADD COLUMN `worker_finish_confirmed` TINYINT(1) NOT NULL DEFAULT 0 AFTER `owner_finish_time`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db
        AND TABLE_NAME = 'repair_order'
        AND COLUMN_NAME = 'worker_finish_time'
    ),
    'SELECT ''worker_finish_time already exists''',
    'ALTER TABLE `repair_order` ADD COLUMN `worker_finish_time` DATETIME DEFAULT NULL AFTER `worker_finish_confirmed`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
