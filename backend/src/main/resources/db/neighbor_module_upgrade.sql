-- Neighbor-help module upgrade script (compatible mode)
-- Execute on existing smart_community database.

SET @db := DATABASE();

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'second_hand' AND COLUMN_NAME = 'community'
    ),
    'SELECT ''second_hand.community exists''',
    'ALTER TABLE `second_hand` ADD COLUMN `community` VARCHAR(50) NOT NULL DEFAULT ''Smart Garden'' AFTER `user_id`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'second_hand' AND COLUMN_NAME = 'description'
    ),
    'SELECT ''second_hand.description exists''',
    'ALTER TABLE `second_hand` ADD COLUMN `description` TEXT DEFAULT NULL AFTER `category`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'second_hand' AND COLUMN_NAME = 'view_count'
    ),
    'SELECT ''second_hand.view_count exists''',
    'ALTER TABLE `second_hand` ADD COLUMN `view_count` INT NOT NULL DEFAULT 0 AFTER `status`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'second_hand' AND COLUMN_NAME = 'report_count'
    ),
    'SELECT ''second_hand.report_count exists''',
    'ALTER TABLE `second_hand` ADD COLUMN `report_count` INT NOT NULL DEFAULT 0 AFTER `view_count`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `second_hand_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `second_hand_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_goods` (`user_id`,`second_hand_id`),
  KEY `idx_goods` (`second_hand_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `second_hand_report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `second_hand_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `reason` VARCHAR(255) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_report_goods` (`second_hand_id`),
  KEY `idx_report_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'lost_found' AND COLUMN_NAME = 'location'
    ),
    'SELECT ''lost_found.location exists''',
    'ALTER TABLE `lost_found` ADD COLUMN `location` VARCHAR(100) DEFAULT NULL AFTER `description`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `lost_found_claim` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `lost_found_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `proof` TEXT DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_claim_lost_found` (`lost_found_id`),
  KEY `idx_claim_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'forum_post' AND COLUMN_NAME = 'view_count'
    ),
    'SELECT ''forum_post.view_count exists''',
    'ALTER TABLE `forum_post` ADD COLUMN `view_count` INT NOT NULL DEFAULT 0 AFTER `content`'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `forum_post_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

