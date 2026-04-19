USE smart_community;

ALTER TABLE `user`
  ADD COLUMN IF NOT EXISTS `points` INT NOT NULL DEFAULT 0 AFTER `phone`;

ALTER TABLE `fee_bill`
  ADD COLUMN IF NOT EXISTS `need_points` INT NOT NULL DEFAULT 0 AFTER `amount`;

UPDATE `fee_bill`
SET `need_points` = CAST(`amount` AS SIGNED)
WHERE `need_points` = 0;

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
  KEY `idx_recharge_create_time` (`create_time`),
  CONSTRAINT `fk_points_recharge_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  CONSTRAINT `fk_points_recharge_operator` FOREIGN KEY (`operator_id`) REFERENCES `user`(`id`)
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
  KEY `idx_consumption_create_time` (`create_time`),
  CONSTRAINT `fk_points_consumption_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
