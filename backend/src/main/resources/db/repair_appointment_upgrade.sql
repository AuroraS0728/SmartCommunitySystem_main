-- Repair appointment upgrade script (add appointment date + time slot)
SET @db := DATABASE();

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'repair_order' AND COLUMN_NAME = 'appointment_date'
    ),
    'SELECT ''appointment_date exists''',
    'ALTER TABLE `repair_order` ADD COLUMN `appointment_date` DATE DEFAULT NULL AFTER `service_sub_type`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'repair_order' AND COLUMN_NAME = 'appointment_time_slot'
    ),
    'SELECT ''appointment_time_slot exists''',
    'ALTER TABLE `repair_order` ADD COLUMN `appointment_time_slot` VARCHAR(20) DEFAULT NULL AFTER `appointment_date`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'repair_order' AND INDEX_NAME = 'idx_appointment_date_slot'
    ),
    'SELECT ''idx_appointment_date_slot exists''',
    'ALTER TABLE `repair_order` ADD KEY `idx_appointment_date_slot` (`appointment_date`,`appointment_time_slot`)'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

