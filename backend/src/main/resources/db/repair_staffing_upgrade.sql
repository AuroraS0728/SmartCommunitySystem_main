-- Repair/housekeeping service and staffing upgrade script (compatible with older MySQL variants)
SET @db := DATABASE();

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.TABLES
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'worker_staffing'
    ),
    'SELECT ''worker_staffing already exists''',
    'CREATE TABLE `worker_staffing` (
      `id` BIGINT NOT NULL AUTO_INCREMENT,
      `worker_id` BIGINT NOT NULL,
      `staff_type` TINYINT NOT NULL DEFAULT 2,
      `position` VARCHAR(50) DEFAULT NULL,
      `shift_group` VARCHAR(20) DEFAULT NULL,
      `certificates` VARCHAR(2000) DEFAULT NULL,
      `specialties` VARCHAR(2000) DEFAULT NULL,
      `max_daily_orders` INT NOT NULL DEFAULT 5,
      `current_status` TINYINT NOT NULL DEFAULT 1,
      `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
      PRIMARY KEY (`id`),
      UNIQUE KEY `uk_worker_id` (`worker_id`),
      KEY `idx_staff_type_status` (`staff_type`,`current_status`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'repair_order' AND COLUMN_NAME = 'service_type'
    ),
    'SELECT ''service_type exists''',
    'ALTER TABLE `repair_order` ADD COLUMN `service_type` TINYINT NOT NULL DEFAULT 1 AFTER `property_id`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'repair_order' AND COLUMN_NAME = 'service_major'
    ),
    'SELECT ''service_major exists''',
    'ALTER TABLE `repair_order` ADD COLUMN `service_major` VARCHAR(50) DEFAULT NULL AFTER `service_type`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'repair_order' AND COLUMN_NAME = 'service_sub_type'
    ),
    'SELECT ''service_sub_type exists''',
    'ALTER TABLE `repair_order` ADD COLUMN `service_sub_type` VARCHAR(100) DEFAULT NULL AFTER `service_major`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'repair_order' AND COLUMN_NAME = 'before_images'
    ),
    'SELECT ''before_images exists''',
    'ALTER TABLE `repair_order` ADD COLUMN `before_images` VARCHAR(2000) DEFAULT NULL AFTER `images`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'repair_order' AND COLUMN_NAME = 'after_images'
    ),
    'SELECT ''after_images exists''',
    'ALTER TABLE `repair_order` ADD COLUMN `after_images` VARCHAR(2000) DEFAULT NULL AFTER `before_images`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'repair_order' AND COLUMN_NAME = 'charge_amount'
    ),
    'SELECT ''charge_amount exists''',
    'ALTER TABLE `repair_order` ADD COLUMN `charge_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER `after_images`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'repair_order' AND COLUMN_NAME = 'charge_remark'
    ),
    'SELECT ''charge_remark exists''',
    'ALTER TABLE `repair_order` ADD COLUMN `charge_remark` VARCHAR(255) DEFAULT NULL AFTER `charge_amount`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'repair_order' AND COLUMN_NAME = 'need_outsource'
    ),
    'SELECT ''need_outsource exists''',
    'ALTER TABLE `repair_order` ADD COLUMN `need_outsource` TINYINT(1) NOT NULL DEFAULT 0 AFTER `charge_remark`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'repair_order' AND INDEX_NAME = 'idx_service_type'
    ),
    'SELECT ''idx_service_type exists''',
    'ALTER TABLE `repair_order` ADD KEY `idx_service_type` (`service_type`)'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

