SET NAMES utf8mb4;
USE smart_community;

SET @idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'user_property' AND index_name = 'idx_user_primary_deleted'
);
SET @idx_sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_user_primary_deleted ON user_property (user_id, is_primary, is_deleted)',
  'SELECT 1'
);
PREPARE stmt FROM @idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'property' AND index_name = 'idx_property_deleted_status'
);
SET @idx_sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_property_deleted_status ON property (is_deleted, status)',
  'SELECT 1'
);
PREPARE stmt FROM @idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'fee_bill' AND index_name = 'idx_fee_property_status_deleted'
);
SET @idx_sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_fee_property_status_deleted ON fee_bill (property_id, status, is_deleted)',
  'SELECT 1'
);
PREPARE stmt FROM @idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'fee_bill' AND index_name = 'idx_fee_due_deleted'
);
SET @idx_sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_fee_due_deleted ON fee_bill (due_date, is_deleted)',
  'SELECT 1'
);
PREPARE stmt FROM @idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'parking_order' AND index_name = 'idx_parking_user_status_deleted'
);
SET @idx_sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_parking_user_status_deleted ON parking_order (user_id, status, is_deleted)',
  'SELECT 1'
);
PREPARE stmt FROM @idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'parking_order' AND index_name = 'idx_parking_payment_status_deleted'
);
SET @idx_sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_parking_payment_status_deleted ON parking_order (payment_time, status, is_deleted)',
  'SELECT 1'
);
PREPARE stmt FROM @idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'repair_order' AND index_name = 'idx_repair_user_status_deleted'
);
SET @idx_sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_repair_user_status_deleted ON repair_order (user_id, status, is_deleted)',
  'SELECT 1'
);
PREPARE stmt FROM @idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'repair_order' AND index_name = 'idx_repair_assignee_status_deleted'
);
SET @idx_sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_repair_assignee_status_deleted ON repair_order (assignee, status, is_deleted)',
  'SELECT 1'
);
PREPARE stmt FROM @idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'notice' AND index_name = 'idx_notice_deleted_publish'
);
SET @idx_sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_notice_deleted_publish ON notice (is_deleted, top, publish_time)',
  'SELECT 1'
);
PREPARE stmt FROM @idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'visitor_invite' AND index_name = 'idx_invite_deleted_expire'
);
SET @idx_sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_invite_deleted_expire ON visitor_invite (is_deleted, expire_time)',
  'SELECT 1'
);
PREPARE stmt FROM @idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
