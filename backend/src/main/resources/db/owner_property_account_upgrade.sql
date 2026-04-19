SET NAMES utf8mb4;
USE smart_community;

ALTER TABLE `user`
  ADD COLUMN IF NOT EXISTS `must_change_password` TINYINT(1) NOT NULL DEFAULT 0 AFTER `password`;

ALTER TABLE `property`
  ADD COLUMN IF NOT EXISTS `property_code` VARCHAR(32) DEFAULT NULL AFTER `room`;

SET @uk_property_code_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'property'
    AND index_name = 'uk_property_code'
);
SET @uk_property_code_sql := IF(
  @uk_property_code_exists = 0,
  'ALTER TABLE `property` ADD UNIQUE KEY `uk_property_code` (`property_code`)',
  'SELECT 1'
);
PREPARE uk_property_code_stmt FROM @uk_property_code_sql;
EXECUTE uk_property_code_stmt;
DEALLOCATE PREPARE uk_property_code_stmt;

UPDATE `property`
SET `property_code` = CONCAT(
    'YZ',
    LPAD(CAST(COALESCE(NULLIF(REGEXP_SUBSTR(`building`, '[0-9]+'), ''), '0') AS UNSIGNED) % 100, 2, '0'),
    LPAD(CAST(COALESCE(NULLIF(REGEXP_SUBSTR(`unit`, '[0-9]+'), ''), '0') AS UNSIGNED) % 100, 2, '0'),
    LPAD(CAST(COALESCE(NULLIF(REGEXP_SUBSTR(`room`, '[0-9]+'), ''), '0') AS UNSIGNED) % 100, 2, '0'),
    DATE_FORMAT(COALESCE(`create_time`, NOW()), '%y')
)
WHERE `property_code` IS NULL OR `property_code` = '';

UPDATE `user` u
JOIN `user_property` up ON up.`user_id` = u.`id`
  AND up.`is_deleted` = 0
  AND up.`is_primary` = 1
JOIN `property` p ON p.`id` = up.`property_id`
  AND p.`is_deleted` = 0
SET u.`account` = p.`property_code`,
    u.`password` = REVERSE(RIGHT(CONCAT(REGEXP_REPLACE(p.`property_code`, '[^0-9]', ''), '000000'), 6)),
    u.`update_time` = NOW()
WHERE u.`role` = 1
  AND p.`property_code` IS NOT NULL
  AND p.`property_code` <> ''
  AND (u.`account` IS NULL OR u.`account` = '' OR u.`account` LIKE 'XQYZ%');

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
