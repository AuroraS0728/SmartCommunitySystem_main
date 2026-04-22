SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS smart_community
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE smart_community;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS complaint;
DROP TABLE IF EXISTS visitor_notify;
DROP TABLE IF EXISTS visitor_blacklist;
DROP TABLE IF EXISTS visitor_invite;
DROP TABLE IF EXISTS access_token;
DROP TABLE IF EXISTS forum_comment;
DROP TABLE IF EXISTS forum_post;
DROP TABLE IF EXISTS forum_post_like;
DROP TABLE IF EXISTS lost_found_claim;
DROP TABLE IF EXISTS lost_found;
DROP TABLE IF EXISTS second_hand_report;
DROP TABLE IF EXISTS second_hand_favorite;
DROP TABLE IF EXISTS second_hand;
DROP TABLE IF EXISTS notice;
DROP TABLE IF EXISTS repair_evaluation;
DROP TABLE IF EXISTS repair_fee_bill;
DROP TABLE IF EXISTS repair_order;
DROP TABLE IF EXISTS worker_staffing;
DROP TABLE IF EXISTS owner_parking_quota;
DROP TABLE IF EXISTS user_vehicle;
DROP TABLE IF EXISTS parking_order;
DROP TABLE IF EXISTS fee_bill;
DROP TABLE IF EXISTS points_consumption_record;
DROP TABLE IF EXISTS points_recharge_record;
DROP TABLE IF EXISTS user_property;
DROP TABLE IF EXISTS property;
DROP TABLE IF EXISTS `user`;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `openid` VARCHAR(64) DEFAULT NULL,
  `unionid` VARCHAR(64) DEFAULT NULL,
  `account` VARCHAR(32) DEFAULT NULL,
  `password` VARCHAR(64) DEFAULT NULL,
  `must_change_password` TINYINT(1) NOT NULL DEFAULT 0,
  `role` TINYINT NOT NULL DEFAULT 1,
  `nickname` VARCHAR(50) DEFAULT NULL,
  `avatar_url` VARCHAR(255) DEFAULT NULL,
  `phone` VARCHAR(32) DEFAULT NULL,
  `points` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  UNIQUE KEY `uk_account` (`account`),
  KEY `idx_phone` (`phone`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `worker_staffing` (
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
  KEY `idx_staff_type_status` (`staff_type`,`current_status`),
  CONSTRAINT `fk_worker_staffing_worker` FOREIGN KEY (`worker_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `property` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `community` VARCHAR(50) NOT NULL,
  `building` VARCHAR(20) NOT NULL,
  `unit` VARCHAR(20) NOT NULL,
  `room` VARCHAR(20) NOT NULL,
  `property_code` VARCHAR(32) DEFAULT NULL,
  `owner_name` VARCHAR(20) NOT NULL,
  `area` DECIMAL(10,2) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_property_code` (`property_code`),
  KEY `idx_building` (`building`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_property` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `property_id` BIGINT NOT NULL,
  `relation` VARCHAR(10) NOT NULL DEFAULT 'family',
  `is_primary` TINYINT(1) NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_property` (`user_id`,`property_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_property_id` (`property_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `fee_bill` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `property_id` BIGINT NOT NULL,
  `bill_period` VARCHAR(10) NOT NULL,
  `area_snapshot` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `unit_price` DECIMAL(10,2) NOT NULL DEFAULT 5.00,
  `amount` DECIMAL(10,2) NOT NULL,
  `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `need_points` INT NOT NULL DEFAULT 0,
  `paid_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `status` TINYINT NOT NULL DEFAULT 0,
  `due_date` DATETIME NOT NULL,
  `payment_time` DATETIME DEFAULT NULL,
  `transaction_id` VARCHAR(64) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transaction_id` (`transaction_id`),
  UNIQUE KEY `uk_property_period` (`property_id`,`bill_period`),
  KEY `idx_property_id` (`property_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `points_recharge_record` (
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

CREATE TABLE `points_consumption_record` (
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

CREATE TABLE `parking_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `property_id` BIGINT DEFAULT NULL,
  `vehicle_no` VARCHAR(10) NOT NULL,
  `order_type` TINYINT NOT NULL,
  `source_type` TINYINT NOT NULL DEFAULT 1,
  `amount` DECIMAL(10,2) NOT NULL,
  `park_hours` INT DEFAULT NULL,
  `free_hours` INT NOT NULL DEFAULT 0,
  `daily_cap` DECIMAL(10,2) NOT NULL DEFAULT 30.00,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  `transaction_id` VARCHAR(64) DEFAULT NULL,
  `payment_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transaction_id` (`transaction_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_vehicle_no` (`vehicle_no`),
  KEY `idx_order_type` (`order_type`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_vehicle` (
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

CREATE TABLE `owner_parking_quota` (
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

CREATE TABLE `repair_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `property_id` BIGINT NOT NULL,
  `service_type` TINYINT NOT NULL DEFAULT 1,
  `service_major` VARCHAR(50) DEFAULT NULL,
  `service_sub_type` VARCHAR(100) DEFAULT NULL,
  `appointment_date` DATE DEFAULT NULL,
  `appointment_time_slot` VARCHAR(20) DEFAULT NULL,
  `category` VARCHAR(100) NOT NULL,
  `description` TEXT NOT NULL,
  `images` VARCHAR(2000) DEFAULT NULL,
  `before_images` VARCHAR(2000) DEFAULT NULL,
  `after_images` VARCHAR(2000) DEFAULT NULL,
  `charge_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `charge_remark` VARCHAR(255) DEFAULT NULL,
  `need_outsource` TINYINT(1) NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `assignee` BIGINT DEFAULT NULL,
  `assigned_time` DATETIME DEFAULT NULL,
  `remark` VARCHAR(255) DEFAULT NULL,
  `owner_finish_confirmed` TINYINT(1) NOT NULL DEFAULT 0,
  `owner_finish_time` DATETIME DEFAULT NULL,
  `worker_finish_confirmed` TINYINT(1) NOT NULL DEFAULT 0,
  `worker_finish_time` DATETIME DEFAULT NULL,
  `completion_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_property_id` (`property_id`),
  KEY `idx_service_type` (`service_type`),
  KEY `idx_appointment_date_slot` (`appointment_date`,`appointment_time_slot`),
  KEY `idx_status` (`status`),
  KEY `idx_assignee` (`assignee`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `repair_fee_bill` (
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

CREATE TABLE `repair_evaluation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `rating` TINYINT NOT NULL,
  `comment` VARCHAR(255) DEFAULT NULL,
  `is_anonymous` TINYINT(1) NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `notice` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(100) NOT NULL,
  `content` LONGTEXT NOT NULL,
  `publisher` VARCHAR(50) NOT NULL,
  `top` TINYINT(1) NOT NULL DEFAULT 0,
  `attachment_urls` VARCHAR(2000) DEFAULT NULL,
  `publish_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_top_publish` (`top`,`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `second_hand` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `community` VARCHAR(50) NOT NULL DEFAULT 'Smart Garden',
  `title` VARCHAR(100) NOT NULL,
  `category` VARCHAR(20) NOT NULL DEFAULT 'other',
  `description` TEXT DEFAULT NULL,
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `images` VARCHAR(2000) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `view_count` INT NOT NULL DEFAULT 0,
  `report_count` INT NOT NULL DEFAULT 0,
  `contact` VARCHAR(50) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `second_hand_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `second_hand_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_goods` (`user_id`,`second_hand_id`),
  KEY `idx_goods` (`second_hand_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `second_hand_report` (
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

CREATE TABLE `lost_found` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `type` TINYINT NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `description` TEXT NOT NULL,
  `location` VARCHAR(100) DEFAULT NULL,
  `contact` VARCHAR(50) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `images` VARCHAR(2000) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `lost_found_claim` (
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

CREATE TABLE `forum_post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `board` VARCHAR(20) NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `content` TEXT NOT NULL,
  `view_count` INT NOT NULL DEFAULT 0,
  `like_cnt` INT NOT NULL DEFAULT 0,
  `reply_cnt` INT NOT NULL DEFAULT 0,
  `is_top` TINYINT(1) NOT NULL DEFAULT 0,
  `is_essence` TINYINT(1) NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_board` (`board`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `forum_post_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `forum_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `parent_id` BIGINT DEFAULT NULL,
  `content` TEXT NOT NULL,
  `like_cnt` INT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `access_token` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `token` VARCHAR(64) NOT NULL,
  `expire_time` DATETIME NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_token` (`token`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `visitor_invite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `host_user_id` BIGINT NOT NULL,
  `visitor_name` VARCHAR(20) NOT NULL,
  `visitor_phone` VARCHAR(20) NOT NULL,
  `code` VARCHAR(64) NOT NULL,
  `validity_type` VARCHAR(20) NOT NULL DEFAULT 'SINGLE_2H',
  `visit_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expire_time` DATETIME NOT NULL,
  `max_uses` INT NOT NULL DEFAULT 1,
  `used_count` INT NOT NULL DEFAULT 0,
  `used_time` DATETIME DEFAULT NULL,
  `share_link` VARCHAR(255) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_host_user_id` (`host_user_id`),
  KEY `idx_invite_expire` (`expire_time`),
  KEY `idx_invite_phone` (`visitor_phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `visitor_blacklist` (
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

CREATE TABLE `visitor_notify` (
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

CREATE TABLE `complaint` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `type` TINYINT NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `content` TEXT NOT NULL,
  `images` VARCHAR(2000) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `reply` TEXT DEFAULT NULL,
  `reply_time` DATETIME DEFAULT NULL,
  `satisfaction` TINYINT DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ===== Seed Data =====
SET NAMES utf8mb4;
USE smart_community;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE complaint;
TRUNCATE TABLE visitor_notify;
TRUNCATE TABLE visitor_blacklist;
TRUNCATE TABLE visitor_invite;
TRUNCATE TABLE access_token;
TRUNCATE TABLE forum_post_like;
TRUNCATE TABLE forum_comment;
TRUNCATE TABLE forum_post;
TRUNCATE TABLE lost_found_claim;
TRUNCATE TABLE lost_found;
TRUNCATE TABLE second_hand_report;
TRUNCATE TABLE second_hand_favorite;
TRUNCATE TABLE second_hand;
TRUNCATE TABLE notice;
TRUNCATE TABLE repair_evaluation;
TRUNCATE TABLE repair_order;
TRUNCATE TABLE worker_staffing;
TRUNCATE TABLE owner_parking_quota;
TRUNCATE TABLE user_vehicle;
TRUNCATE TABLE parking_order;
TRUNCATE TABLE fee_bill;
TRUNCATE TABLE points_consumption_record;
TRUNCATE TABLE points_recharge_record;
TRUNCATE TABLE user_property;
TRUNCATE TABLE property;
TRUNCATE TABLE `user`;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO `user` (`id`, `openid`, `unionid`, `account`, `password`, `must_change_password`, `role`, `nickname`, `avatar_url`, `phone`, `points`, `status`, `create_time`, `update_time`, `is_deleted`) VALUES
(1, 'openid_owner_001', 'unionid_001', CONCAT('YZ0101101', DATE_FORMAT(NOW(), '%y')), REVERSE(RIGHT(CONCAT('000000', REGEXP_REPLACE(CONCAT('YZ0101101', DATE_FORMAT(NOW(), '%y')), '[^0-9]', '')), 6)), 1, 1, 'Owner-A', NULL, '13800000001', 1000, 1, NOW() - INTERVAL 40 DAY, NOW() - INTERVAL 40 DAY, 0),
(2, 'openid_owner_002', 'unionid_002', CONCAT('YZ0101102', DATE_FORMAT(NOW(), '%y')), REVERSE(RIGHT(CONCAT('000000', REGEXP_REPLACE(CONCAT('YZ0101102', DATE_FORMAT(NOW(), '%y')), '[^0-9]', '')), 6)), 1, 1, 'Owner-B', NULL, '13800000002', 500, 1, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 20 DAY, 0),
(3, 'openid_owner_003', 'unionid_003', CONCAT('YZ0201201', DATE_FORMAT(NOW(), '%y')), REVERSE(RIGHT(CONCAT('000000', REGEXP_REPLACE(CONCAT('YZ0201201', DATE_FORMAT(NOW(), '%y')), '[^0-9]', '')), 6)), 1, 1, 'Owner-C', NULL, '13800000003', 0, 1, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 0),
(10, 'openid_admin_001', 'unionid_010', 'WTGL456789', '987654', 0, 2, 'Admin-A', NULL, 'ENC_13900000010', 0, 1, NOW() - INTERVAL 100 DAY, NOW() - INTERVAL 100 DAY, 0),
(20, 'openid_worker_001', 'unionid_020', 'JZWX567890', '098765', 0, 3, 'Worker-A', NULL, '13700000020', 0, 1, NOW() - INTERVAL 80 DAY, NOW() - INTERVAL 80 DAY, 0);

INSERT INTO `worker_staffing` (`id`, `worker_id`, `staff_type`, `position`, `shift_group`, `certificates`, `specialties`, `max_daily_orders`, `current_status`, `create_time`, `update_time`, `is_deleted`) VALUES
(1, 20, 2, 'Repair-Plumber', 'A', 'plumber_cert', 'water_pipe,drainage', 8, 1, NOW() - INTERVAL 80 DAY, NOW() - INTERVAL 1 DAY, 0);

INSERT INTO `property` (`id`, `community`, `building`, `unit`, `room`, `property_code`, `owner_name`, `area`, `status`, `create_time`, `update_time`, `is_deleted`) VALUES
(1001, 'Smart Garden', '1', '1', '101', CONCAT('YZ0101101', DATE_FORMAT(NOW(), '%y')), 'Owner-A', 98.50, 4, NOW() - INTERVAL 50 DAY, NOW() - INTERVAL 50 DAY, 0),
(1002, 'Smart Garden', '1', '1', '102', CONCAT('YZ0101102', DATE_FORMAT(NOW(), '%y')), 'Owner-B', 88.20, 4, NOW() - INTERVAL 49 DAY, NOW() - INTERVAL 49 DAY, 0),
(1003, 'Smart Garden', '2', '1', '201', CONCAT('YZ0201201', DATE_FORMAT(NOW(), '%y')), '', 108.30, 3, NOW() - INTERVAL 48 DAY, NOW() - INTERVAL 48 DAY, 0);

INSERT INTO `user_property` (`id`, `user_id`, `property_id`, `relation`, `is_primary`, `create_time`, `update_time`, `is_deleted`) VALUES
(2001, 1, 1001, 'self', 1, NOW() - INTERVAL 40 DAY, NOW() - INTERVAL 40 DAY, 0),
(2002, 2, 1002, 'self', 1, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 20 DAY, 0),
(2003, 3, 1003, 'family', 0, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 0);

INSERT INTO `fee_bill` (`id`, `property_id`, `bill_period`, `area_snapshot`, `unit_price`, `amount`, `discount_amount`, `need_points`, `paid_amount`, `status`, `due_date`, `payment_time`, `transaction_id`, `create_time`, `update_time`, `is_deleted`) VALUES
(3001, 1001, DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m'), 98.50, 5.00, 492.50, 0.00, 493, 492.50, 2, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 18 DAY, 'TXN_FEE_3001', NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 18 DAY, 0),
(3002, 1002, DATE_FORMAT(NOW(), '%Y-%m'), 88.20, 5.00, 441.00, 0.00, 441, 100.00, 1, NOW() + INTERVAL 8 DAY, NOW() - INTERVAL 1 DAY, 'TXN_FEE_3002', NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 1 DAY, 0),
(3003, 1003, DATE_FORMAT(NOW(), '%Y-%m'), 108.30, 5.00, 541.50, 0.00, 542, 0.00, 0, NOW() - INTERVAL 2 DAY, NULL, NULL, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY, 0);

INSERT INTO `parking_order` (`id`, `user_id`, `property_id`, `vehicle_no`, `order_type`, `source_type`, `amount`, `park_hours`, `free_hours`, `daily_cap`, `start_time`, `end_time`, `status`, `transaction_id`, `payment_time`, `create_time`, `update_time`, `is_deleted`) VALUES
(4001, 1, 1001, 'A12345', 2, 1, 500.00, NULL, 0, 30.00, NOW() - INTERVAL 20 DAY, NOW() + INTERVAL 10 DAY, 1, 'TXN_PARK_4001', NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 20 DAY, 0),
(4002, 2, 1002, 'B67890', 1, 1, 20.00, 10, 0, 30.00, NOW() - INTERVAL 1 DAY, NOW(), 0, NULL, NULL, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 0);

INSERT INTO `user_vehicle` (`id`, `user_id`, `vehicle_no`, `is_visitor`, `host_user_id`, `parking_deadline`, `remind_time`, `status`, `create_time`, `update_time`, `is_deleted`) VALUES
(15001, 1, 'A12345', 0, NULL, NULL, NULL, 1, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY, 0),
(15002, 2, 'B67890', 0, NULL, NULL, NULL, 1, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 20 DAY, 0),
(15003, 1, 'V55555', 1, 1, NOW() + INTERVAL 5 HOUR, NOW() + INTERVAL 4 HOUR, 1, NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 1 HOUR, 0);

INSERT INTO `owner_parking_quota` (`id`, `user_id`, `month_key`, `free_hours_total`, `free_hours_used`, `owner_extra_hours`, `create_time`, `update_time`) VALUES
(16001, 1, DATE_FORMAT(NOW(), '%Y-%m'), 10, 2, 3, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 1 DAY),
(16002, 2, DATE_FORMAT(NOW(), '%Y-%m'), 10, 0, 0, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY);

INSERT INTO `repair_order` (`id`, `user_id`, `property_id`, `category`, `description`, `images`, `status`, `assignee`, `assigned_time`, `remark`, `completion_time`, `create_time`, `update_time`, `is_deleted`) VALUES
(5001, 1, 1001, 'electrical', 'Living room light flickers', '[]', 1, NULL, NULL, NULL, NULL, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 0),
(5002, 2, 1002, 'plumbing', 'Kitchen sink leak', '[]', 4, 20, NOW() - INTERVAL 4 DAY, 'completed', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 3 DAY, 0),
(5003, 1, 1001, 'door', 'Door lock jammed', '[]', 2, 20, NOW() - INTERVAL 2 DAY, 'processing', NULL, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 0);

INSERT INTO `repair_evaluation` (`id`, `order_id`, `rating`, `comment`, `is_anonymous`, `create_time`, `update_time`, `is_deleted`) VALUES
(6001, 5002, 5, 'Very fast service', 0, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 0);

INSERT INTO `notice` (`id`, `title`, `content`, `publisher`, `top`, `attachment_urls`, `publish_time`, `create_time`, `update_time`, `is_deleted`) VALUES
(7001, 'Elevator Maintenance', 'Building 1 elevator maintenance this weekend.', 'Property Center', 1, NULL, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 0),
(7002, 'Pest Control', 'Public area disinfection on Friday.', 'Property Center', 0, NULL, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 0),
(7003, 'Parking Upgrade', 'Parking gate system will be upgraded tonight.', 'Property Center', 0, NULL, NOW(), NOW(), NOW(), 0);

INSERT INTO `second_hand` (`id`, `user_id`, `title`, `category`, `price`, `images`, `status`, `contact`, `create_time`, `update_time`, `is_deleted`) VALUES
(8001, 1, 'Baby Stroller', 'kids', 280.00, '[]', 1, '13800000001', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 0),
(8002, 2, 'Desk Lamp', 'home', 60.00, '[]', 2, '13800000002', NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 2 DAY, 0);

INSERT INTO `lost_found` (`id`, `user_id`, `type`, `title`, `description`, `contact`, `status`, `images`, `create_time`, `update_time`, `is_deleted`) VALUES
(9001, 1, 1, 'Lost key card', 'Lost near building 1 gate', '13800000001', 1, '[]', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 0),
(9002, 2, 2, 'Found keychain', 'Found in public garden', '13800000002', 1, '[]', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 0);

INSERT INTO `forum_post` (`id`, `user_id`, `board`, `title`, `content`, `like_cnt`, `reply_cnt`, `is_top`, `is_essence`, `create_time`, `update_time`, `is_deleted`) VALUES
(10001, 1, 'chat', 'Weekend activity', 'Anyone join kids activity this weekend?', 5, 1, 0, 0, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 0),
(10002, 2, 'help', 'Need a drill', 'Can someone lend me a drill for half a day?', 8, 1, 0, 1, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 0);

INSERT INTO `forum_comment` (`id`, `post_id`, `user_id`, `parent_id`, `content`, `like_cnt`, `create_time`, `update_time`, `is_deleted`) VALUES
(11001, 10001, 2, NULL, 'We can join.', 1, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 0),
(11002, 10002, 3, NULL, 'I have one drill.', 2, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 0);

INSERT INTO `access_token` (`id`, `user_id`, `token`, `expire_time`, `status`, `create_time`, `update_time`, `is_deleted`) VALUES
(12001, 1, 'ACC_0001', NOW() + INTERVAL 20 MINUTE, 1, NOW() - INTERVAL 5 MINUTE, NOW() - INTERVAL 5 MINUTE, 0),
(12002, 2, 'ACC_0002', NOW() - INTERVAL 10 MINUTE, 3, NOW() - INTERVAL 40 MINUTE, NOW() - INTERVAL 10 MINUTE, 0);

INSERT INTO `visitor_invite` (`id`, `host_user_id`, `visitor_name`, `visitor_phone`, `code`, `expire_time`, `max_uses`, `used_count`, `used_time`, `create_time`, `update_time`, `is_deleted`) VALUES
(13001, 1, 'Visitor-A', '13600000001', 'INV-A001', NOW() + INTERVAL 6 HOUR, 1, 0, NULL, NOW() - INTERVAL 30 MINUTE, NOW() - INTERVAL 30 MINUTE, 0),
(13002, 2, 'Visitor-B', '13600000002', 'INV-B001', NOW() + INTERVAL 1 DAY, 2, 1, NOW() - INTERVAL 20 MINUTE, NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 20 MINUTE, 0),
(13003, 1, 'Visitor-C', '13600000003', 'INV-C001', NOW() - INTERVAL 1 HOUR, 1, 0, NULL, NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 3 HOUR, 0);

INSERT INTO `visitor_blacklist` (`id`, `phone`, `reason`, `created_by`, `create_time`, `update_time`, `is_deleted`) VALUES
(13501, '13600009999', 'multiple invalid entries', 10, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 0);

INSERT INTO `visitor_notify` (`id`, `host_user_id`, `invite_id`, `visitor_name`, `content`, `read_flag`, `create_time`, `update_time`, `is_deleted`) VALUES
(13601, 2, 13002, 'Visitor-B', '鎮ㄧ殑璁垮銆怴isitor-B銆戝凡杩涘叆灏忓尯', 0, NOW() - INTERVAL 20 MINUTE, NOW() - INTERVAL 20 MINUTE, 0);

INSERT INTO `complaint` (`id`, `user_id`, `type`, `title`, `content`, `images`, `status`, `reply`, `reply_time`, `satisfaction`, `create_time`, `update_time`, `is_deleted`) VALUES
(14001, 1, 1, 'Night noise', 'Construction noise after 22:00', '[]', 3, 'Construction adjusted to daytime.', NOW() - INTERVAL 2 DAY, 3, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 2 DAY, 0),
(14002, 2, 2, 'Add parcel locker', 'Suggest adding parcel locker near building 2.', '[]', 1, NULL, NULL, NULL, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 0);

