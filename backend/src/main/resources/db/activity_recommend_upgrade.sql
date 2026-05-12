CREATE TABLE IF NOT EXISTS `recommend_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `rule_name` VARCHAR(100) NOT NULL,
  `service_id` VARCHAR(100) NOT NULL,
  `service_name` VARCHAR(100) NOT NULL,
  `rule_expression` VARCHAR(255) NOT NULL,
  `condition_json` TEXT NULL,
  `image_url` VARCHAR(300) NULL,
  `price` INT NOT NULL DEFAULT 0,
  `priority` INT NOT NULL DEFAULT 99,
  `enabled` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `activity` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(120) NOT NULL,
  `description` TEXT NULL,
  `type` VARCHAR(40) NOT NULL,
  `image_url` VARCHAR(300) NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NOT NULL,
  `location` VARCHAR(200) NOT NULL,
  `max_participants` INT NOT NULL DEFAULT 0,
  `current_participants` INT NOT NULL DEFAULT 0,
  `age_limit` VARCHAR(50) NULL,
  `with_child_required` TINYINT NOT NULL DEFAULT 0,
  `with_pet_required` TINYINT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `activity_registration` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `activity_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `nickname` VARCHAR(80) NOT NULL,
  `phone` VARCHAR(30) NOT NULL,
  `age` INT NOT NULL DEFAULT 0,
  `has_child` TINYINT NOT NULL DEFAULT 0,
  `has_pet` TINYINT NOT NULL DEFAULT 0,
  `remark` VARCHAR(300) NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_activity_registration_activity` (`activity_id`),
  KEY `idx_activity_registration_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
