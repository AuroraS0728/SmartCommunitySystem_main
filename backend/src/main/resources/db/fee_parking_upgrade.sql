USE smart_community;

ALTER TABLE `fee_bill`
  ADD COLUMN IF NOT EXISTS `area_snapshot` DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER `bill_period`,
  ADD COLUMN IF NOT EXISTS `unit_price` DECIMAL(10,2) NOT NULL DEFAULT 5.00 AFTER `area_snapshot`,
  ADD COLUMN IF NOT EXISTS `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER `amount`;

UPDATE `fee_bill` fb
LEFT JOIN `property` p ON p.id = fb.property_id
SET fb.`area_snapshot` = IFNULL(p.`area`, fb.`area_snapshot`),
    fb.`unit_price` = IF(fb.`unit_price` IS NULL OR fb.`unit_price` = 0, 5.00, fb.`unit_price`);

UPDATE `fee_bill`
SET `need_points` = CEILING(`amount`)
WHERE `need_points` = 0;

CREATE UNIQUE INDEX IF NOT EXISTS `uk_property_period` ON `fee_bill` (`property_id`, `bill_period`);

ALTER TABLE `parking_order`
  ADD COLUMN IF NOT EXISTS `source_type` TINYINT NOT NULL DEFAULT 1 AFTER `order_type`,
  ADD COLUMN IF NOT EXISTS `park_hours` INT DEFAULT NULL AFTER `amount`,
  ADD COLUMN IF NOT EXISTS `free_hours` INT NOT NULL DEFAULT 0 AFTER `park_hours`,
  ADD COLUMN IF NOT EXISTS `daily_cap` DECIMAL(10,2) NOT NULL DEFAULT 30.00 AFTER `free_hours`,
  ADD COLUMN IF NOT EXISTS `payment_time` DATETIME DEFAULT NULL AFTER `transaction_id`;

CREATE INDEX IF NOT EXISTS `idx_order_type` ON `parking_order` (`order_type`, `status`);

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
