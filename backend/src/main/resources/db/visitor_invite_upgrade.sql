USE smart_community;

ALTER TABLE `visitor_invite`
  ADD COLUMN IF NOT EXISTS `validity_type` VARCHAR(20) NOT NULL DEFAULT 'SINGLE_2H' AFTER `code`,
  ADD COLUMN IF NOT EXISTS `visit_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `validity_type`,
  ADD COLUMN IF NOT EXISTS `share_link` VARCHAR(255) DEFAULT NULL AFTER `used_time`;

CREATE INDEX IF NOT EXISTS `idx_invite_expire` ON `visitor_invite` (`expire_time`);
CREATE INDEX IF NOT EXISTS `idx_invite_phone` ON `visitor_invite` (`visitor_phone`);

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
