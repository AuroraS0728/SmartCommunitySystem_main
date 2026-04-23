-- Repair multi-worker and fee objection upgrade
-- Execute this script on existing databases.

SET @db := DATABASE();

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db
        AND TABLE_NAME = 'repair_order'
        AND COLUMN_NAME = 'service_start_time'
    ),
    'SELECT ''service_start_time already exists''',
    'ALTER TABLE `repair_order` ADD COLUMN `service_start_time` DATETIME DEFAULT NULL AFTER `worker_finish_time`'
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
        AND COLUMN_NAME = 'service_end_time'
    ),
    'SELECT ''service_end_time already exists''',
    'ALTER TABLE `repair_order` ADD COLUMN `service_end_time` DATETIME DEFAULT NULL AFTER `service_start_time`'
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
        AND COLUMN_NAME = 'service_duration_minutes'
    ),
    'SELECT ''service_duration_minutes already exists''',
    'ALTER TABLE `repair_order` ADD COLUMN `service_duration_minutes` INT NOT NULL DEFAULT 0 AFTER `service_end_time`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `repair_order_worker` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `worker_id` BIGINT NOT NULL,
  `role_type` TINYINT NOT NULL DEFAULT 1,
  `verify_passed` TINYINT(1) NOT NULL DEFAULT 0,
  `verify_pass_time` DATETIME DEFAULT NULL,
  `finish_confirmed` TINYINT(1) NOT NULL DEFAULT 0,
  `finish_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_worker` (`order_id`,`worker_id`),
  KEY `idx_worker_id` (`worker_id`),
  KEY `idx_order_verify` (`order_id`,`verify_passed`),
  KEY `idx_order_finish` (`order_id`,`finish_confirmed`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `repair_fee_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `worker_id` BIGINT NOT NULL,
  `tech_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `material_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `high_altitude_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `other_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `remark` VARCHAR(255) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_worker` (`order_id`,`worker_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_worker_id` (`worker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `repair_fee_objection` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `bill_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `deposit_points` INT NOT NULL DEFAULT 0,
  `reason` VARCHAR(500) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  `resolution_remark` VARCHAR(500) DEFAULT NULL,
  `resolver_id` BIGINT DEFAULT NULL,
  `refund_points` INT NOT NULL DEFAULT 0,
  `resolve_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_order_status` (`order_id`,`status`),
  KEY `idx_user_status` (`user_id`,`status`),
  KEY `idx_bill_id` (`bill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Bootstrap participant rows from historical single-assignee orders.
INSERT INTO `repair_order_worker` (`order_id`, `worker_id`, `role_type`, `verify_passed`, `finish_confirmed`, `create_time`, `update_time`, `is_deleted`)
SELECT r.id, r.assignee, 1, 0, IFNULL(r.worker_finish_confirmed, 0), NOW(), NOW(), 0
FROM `repair_order` r
WHERE r.assignee IS NOT NULL
  AND r.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1
    FROM `repair_order_worker` w
    WHERE w.order_id = r.id
      AND w.worker_id = r.assignee
      AND w.is_deleted = 0
  );

