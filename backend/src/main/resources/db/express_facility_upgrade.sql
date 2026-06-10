CREATE TABLE IF NOT EXISTS `express_package` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `property_id` BIGINT NOT NULL,
  `courier_company` VARCHAR(50) NOT NULL,
  `tracking_no` VARCHAR(64) NOT NULL,
  `pickup_code` VARCHAR(32) DEFAULT NULL,
  `recipient_name` VARCHAR(50) DEFAULT NULL,
  `recipient_phone` VARCHAR(32) DEFAULT NULL,
  `shelf_location` VARCHAR(100) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  `arrived_time` DATETIME NOT NULL,
  `pickup_time` DATETIME DEFAULT NULL,
  `remark` VARCHAR(255) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_express_property` (`property_id`),
  KEY `idx_express_tracking` (`tracking_no`),
  KEY `idx_express_status_arrived` (`status`,`arrived_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `facility_info` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `category` VARCHAR(50) NOT NULL,
  `location` VARCHAR(100) DEFAULT NULL,
  `open_hours` VARCHAR(100) DEFAULT NULL,
  `contact_phone` VARCHAR(32) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `sort_order` INT NOT NULL DEFAULT 0,
  `description` VARCHAR(1000) DEFAULT NULL,
  `image_urls` VARCHAR(2000) DEFAULT NULL,
  `last_inspection_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_facility_status_sort` (`status`,`sort_order`),
  KEY `idx_facility_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @db := DATABASE();

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'repair_order' AND COLUMN_NAME = 'facility_id'
    ),
    'SELECT 1',
    'ALTER TABLE `repair_order` ADD COLUMN `facility_id` BIGINT DEFAULT NULL AFTER `service_sub_type`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'repair_order' AND COLUMN_NAME = 'facility_name'
    ),
    'SELECT 1',
    'ALTER TABLE `repair_order` ADD COLUMN `facility_name` VARCHAR(100) DEFAULT NULL AFTER `facility_id`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
