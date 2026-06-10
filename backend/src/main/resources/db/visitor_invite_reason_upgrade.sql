SET NAMES utf8mb4;
USE smart_community;

SET @db := DATABASE();

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'visitor_invite' AND COLUMN_NAME = 'visit_reason'
    ),
    'SELECT 1',
    'ALTER TABLE `visitor_invite` ADD COLUMN `visit_reason` VARCHAR(255) DEFAULT NULL AFTER `visitor_phone`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
