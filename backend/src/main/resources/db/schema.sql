SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS smart_community
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE smart_community;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS complaint;
DROP TABLE IF EXISTS activity_registration;
DROP TABLE IF EXISTS activity;
DROP TABLE IF EXISTS recommend_rule;
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
DROP TABLE IF EXISTS additional_service_order;
DROP TABLE IF EXISTS repair_fee_objection;
DROP TABLE IF EXISTS repair_fee_detail;
DROP TABLE IF EXISTS repair_order_worker;
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
  `service_start_time` DATETIME DEFAULT NULL,
  `service_end_time` DATETIME DEFAULT NULL,
  `service_duration_minutes` INT NOT NULL DEFAULT 0,
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

CREATE TABLE `repair_order_worker` (
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

CREATE TABLE `repair_fee_detail` (
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

CREATE TABLE `repair_fee_objection` (
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

CREATE TABLE `additional_service_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `service_id` VARCHAR(100) NOT NULL,
  `service_name` VARCHAR(100) NOT NULL,
  `price` INT NOT NULL DEFAULT 0,
  `points_cost` INT NOT NULL DEFAULT 0,
  `appointment_date` DATE NOT NULL,
  `appointment_time_slot` VARCHAR(20) NOT NULL,
  `contact_name` VARCHAR(50) DEFAULT NULL,
  `contact_phone` VARCHAR(32) DEFAULT NULL,
  `remark` VARCHAR(200) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `paid_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_additional_service_user` (`user_id`),
  KEY `idx_additional_service_status` (`status`),
  KEY `idx_additional_service_service` (`service_id`),
  KEY `idx_additional_service_create_time` (`create_time`)
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
