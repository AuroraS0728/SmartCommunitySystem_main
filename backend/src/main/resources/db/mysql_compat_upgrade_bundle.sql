SET NAMES utf8mb4;
USE smart_community;

SET @db := DATABASE();

-- user: account/password/must_change_password
SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user' AND COLUMN_NAME = 'account'
    ),
    'SELECT 1',
    'ALTER TABLE `user` ADD COLUMN `account` VARCHAR(32) DEFAULT NULL COMMENT ''Login account'''
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
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user' AND COLUMN_NAME = 'password'
    ),
    'SELECT 1',
    'ALTER TABLE `user` ADD COLUMN `password` VARCHAR(64) DEFAULT NULL COMMENT ''Login password'''
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
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user' AND COLUMN_NAME = 'must_change_password'
    ),
    'SELECT 1',
    'ALTER TABLE `user` ADD COLUMN `must_change_password` TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''first login must change password'''
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- property: property_code
SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'property' AND COLUMN_NAME = 'property_code'
    ),
    'SELECT 1',
    'ALTER TABLE `property` ADD COLUMN `property_code` VARCHAR(32) DEFAULT NULL COMMENT ''Property code'''
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- fee_bill columns
SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'fee_bill' AND COLUMN_NAME = 'area_snapshot'
    ),
    'SELECT 1',
    'ALTER TABLE `fee_bill` ADD COLUMN `area_snapshot` DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER `bill_period`'
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
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'fee_bill' AND COLUMN_NAME = 'unit_price'
    ),
    'SELECT 1',
    'ALTER TABLE `fee_bill` ADD COLUMN `unit_price` DECIMAL(10,2) NOT NULL DEFAULT 5.00 AFTER `area_snapshot`'
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
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'fee_bill' AND COLUMN_NAME = 'discount_amount'
    ),
    'SELECT 1',
    'ALTER TABLE `fee_bill` ADD COLUMN `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER `amount`'
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
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'fee_bill' AND COLUMN_NAME = 'need_points'
    ),
    'SELECT 1',
    'ALTER TABLE `fee_bill` ADD COLUMN `need_points` INT NOT NULL DEFAULT 0 AFTER `amount`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- parking_order columns
SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'parking_order' AND COLUMN_NAME = 'source_type'
    ),
    'SELECT 1',
    'ALTER TABLE `parking_order` ADD COLUMN `source_type` TINYINT NOT NULL DEFAULT 1 AFTER `order_type`'
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
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'parking_order' AND COLUMN_NAME = 'park_hours'
    ),
    'SELECT 1',
    'ALTER TABLE `parking_order` ADD COLUMN `park_hours` INT DEFAULT NULL AFTER `amount`'
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
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'parking_order' AND COLUMN_NAME = 'free_hours'
    ),
    'SELECT 1',
    'ALTER TABLE `parking_order` ADD COLUMN `free_hours` INT NOT NULL DEFAULT 0 AFTER `park_hours`'
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
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'parking_order' AND COLUMN_NAME = 'daily_cap'
    ),
    'SELECT 1',
    'ALTER TABLE `parking_order` ADD COLUMN `daily_cap` DECIMAL(10,2) NOT NULL DEFAULT 30.00 AFTER `free_hours`'
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
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'parking_order' AND COLUMN_NAME = 'payment_time'
    ),
    'SELECT 1',
    'ALTER TABLE `parking_order` ADD COLUMN `payment_time` DATETIME DEFAULT NULL AFTER `transaction_id`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- visitor_invite columns
SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'visitor_invite' AND COLUMN_NAME = 'validity_type'
    ),
    'SELECT 1',
    'ALTER TABLE `visitor_invite` ADD COLUMN `validity_type` VARCHAR(20) NOT NULL DEFAULT ''SINGLE_2H'' AFTER `code`'
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
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'visitor_invite' AND COLUMN_NAME = 'visit_time'
    ),
    'SELECT 1',
    'ALTER TABLE `visitor_invite` ADD COLUMN `visit_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `validity_type`'
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
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'visitor_invite' AND COLUMN_NAME = 'share_link'
    ),
    'SELECT 1',
    'ALTER TABLE `visitor_invite` ADD COLUMN `share_link` VARCHAR(255) DEFAULT NULL AFTER `used_time`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- tables
CREATE TABLE IF NOT EXISTS `points_recharge_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `operator_id` BIGINT NOT NULL,
  `amount` INT NOT NULL,
  `before_points` INT NOT NULL,
  `after_points` INT NOT NULL,
  `remark` VARCHAR(255) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_recharge_user_id` (`user_id`),
  KEY `idx_recharge_operator_id` (`operator_id`),
  KEY `idx_recharge_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `points_consumption_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `business_type` TINYINT NOT NULL,
  `business_id` BIGINT NOT NULL,
  `points` INT NOT NULL,
  `before_points` INT NOT NULL,
  `after_points` INT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_consumption_user_id` (`user_id`),
  KEY `idx_consumption_business` (`business_type`,`business_id`),
  KEY `idx_consumption_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_vehicle` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `vehicle_no` VARCHAR(10) NOT NULL,
  `is_visitor` TINYINT(1) NOT NULL DEFAULT 0,
  `host_user_id` BIGINT DEFAULT NULL,
  `parking_deadline` DATETIME DEFAULT NULL,
  `remind_time` DATETIME DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_vehicle` (`user_id`,`vehicle_no`,`is_visitor`),
  KEY `idx_vehicle_no` (`vehicle_no`),
  KEY `idx_host_user` (`host_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `owner_parking_quota` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `month_key` VARCHAR(7) NOT NULL,
  `free_hours_total` INT NOT NULL DEFAULT 10,
  `free_hours_used` INT NOT NULL DEFAULT 0,
  `owner_extra_hours` INT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_owner_month` (`user_id`,`month_key`),
  KEY `idx_month_key` (`month_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `visitor_blacklist` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `phone` VARCHAR(20) NOT NULL,
  `reason` VARCHAR(255) DEFAULT NULL,
  `created_by` BIGINT DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_blacklist_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `visitor_notify` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `host_user_id` BIGINT NOT NULL,
  `invite_id` BIGINT NOT NULL,
  `visitor_name` VARCHAR(20) NOT NULL,
  `content` VARCHAR(255) NOT NULL,
  `read_flag` TINYINT(1) NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_notify_host` (`host_user_id`),
  KEY `idx_notify_invite` (`invite_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `repair_fee_bill` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `property_id` BIGINT NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `need_points` INT NOT NULL DEFAULT 0,
  `paid_points` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 0,
  `due_date` DATETIME DEFAULT NULL,
  `payment_time` DATETIME DEFAULT NULL,
  `transaction_id` VARCHAR(64) DEFAULT NULL,
  `remark` VARCHAR(255) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  UNIQUE KEY `uk_transaction_id` (`transaction_id`),
  KEY `idx_user_status` (`user_id`,`status`),
  KEY `idx_property_status` (`property_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- indexes
SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'fee_bill' AND INDEX_NAME = 'uk_property_period'
    ),
    'SELECT 1',
    'CREATE UNIQUE INDEX `uk_property_period` ON `fee_bill`(`property_id`,`bill_period`)'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'parking_order' AND INDEX_NAME = 'idx_order_type'
    ),
    'SELECT 1',
    'CREATE INDEX `idx_order_type` ON `parking_order`(`order_type`,`status`)'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'visitor_invite' AND INDEX_NAME = 'idx_invite_expire'
    ),
    'SELECT 1',
    'CREATE INDEX `idx_invite_expire` ON `visitor_invite`(`expire_time`)'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'visitor_invite' AND INDEX_NAME = 'idx_invite_phone'
    ),
    'SELECT 1',
    'CREATE INDEX `idx_invite_phone` ON `visitor_invite`(`visitor_phone`)'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user' AND INDEX_NAME = 'uk_account'
    ),
    'SELECT 1',
    'ALTER TABLE `user` ADD UNIQUE KEY `uk_account`(`account`)'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'property' AND INDEX_NAME = 'uk_property_code'
    ),
    'SELECT 1',
    'ALTER TABLE `property` ADD UNIQUE KEY `uk_property_code`(`property_code`)'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- data backfill
UPDATE `fee_bill` fb
LEFT JOIN `property` p ON p.id = fb.property_id
SET fb.`area_snapshot` = IFNULL(p.`area`, fb.`area_snapshot`),
    fb.`unit_price` = IF(fb.`unit_price` IS NULL OR fb.`unit_price` = 0, 5.00, fb.`unit_price`);

UPDATE `fee_bill`
SET `need_points` = CEILING(`amount`)
WHERE `need_points` = 0;

UPDATE `property`
SET `property_code` = CONCAT(
    'YZ',
    LPAD(CAST(COALESCE(NULLIF(REGEXP_SUBSTR(`building`, '[0-9]+'), ''), '0') AS UNSIGNED) % 100, 2, '0'),
    LPAD(CAST(COALESCE(NULLIF(REGEXP_SUBSTR(`unit`, '[0-9]+'), ''), '0') AS UNSIGNED) % 100, 2, '0'),
    LPAD(CAST(COALESCE(NULLIF(REGEXP_SUBSTR(`room`, '[0-9]+'), ''), '0') AS UNSIGNED) % 1000, 3, '0'),
    DATE_FORMAT(COALESCE(`create_time`, NOW()), '%y')
)
WHERE `is_deleted` = 0
  AND (`property_code` IS NULL OR `property_code` = '' OR `property_code` NOT REGEXP '^YZ[0-9]{9}$');

UPDATE `user` u
JOIN `user_property` up ON up.`user_id` = u.`id` AND up.`is_deleted` = 0 AND up.`is_primary` = 1
JOIN `property` p ON p.`id` = up.`property_id` AND p.`is_deleted` = 0
SET u.`account` = p.`property_code`,
    u.`update_time` = NOW()
WHERE u.`role` = 1
  AND u.`is_deleted` = 0
  AND p.`property_code` IS NOT NULL
  AND p.`property_code` <> '';

UPDATE `user` u
JOIN `user_property` up ON up.`user_id` = u.`id` AND up.`is_deleted` = 0 AND up.`is_primary` = 1
JOIN `property` p ON p.`id` = up.`property_id` AND p.`is_deleted` = 0
SET u.`password` = REVERSE(RIGHT(CONCAT(REGEXP_REPLACE(COALESCE(u.`account`, ''), '[^0-9]', ''), '000000'), 6)),
    u.`must_change_password` = 1,
    u.`update_time` = NOW()
WHERE u.`role` = 1
  AND u.`is_deleted` = 0
  AND p.`property_code` IS NOT NULL
  AND p.`property_code` <> ''
  AND u.`account` IS NOT NULL
  AND u.`account` <> ''
  AND (u.`password` IS NULL OR u.`password` = '');
