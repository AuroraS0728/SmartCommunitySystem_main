-- Innovation features upgrade for MySQL 8.0
-- Adds complaint analysis, credit, owner profile, dispatch/SLA, reminders, and property tasks.

CREATE TABLE IF NOT EXISTS `emergency_keyword` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `keyword` VARCHAR(20) NOT NULL COMMENT '紧急关键词',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emergency_keyword` (`keyword`),
  KEY `idx_emergency_keyword_enabled` (`enabled`),
  KEY `idx_emergency_keyword_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='紧急关键词表';

CREATE TABLE IF NOT EXISTS `credit_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `change_value` INT NOT NULL,
  `reason` VARCHAR(100) DEFAULT NULL,
  `before_score` INT DEFAULT NULL,
  `after_score` INT DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_credit_log_user_id` (`user_id`),
  KEY `idx_credit_log_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='信用变更日志表';

CREATE TABLE IF NOT EXISTS `payment_reminder` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `fee_bill_id` BIGINT NOT NULL,
  `method` VARCHAR(20) DEFAULT NULL COMMENT 'MESSAGE/TASK',
  `content` VARCHAR(200) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待发送1已发送',
  `send_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_payment_reminder_user_id` (`user_id`),
  KEY `idx_payment_reminder_fee_bill_id` (`fee_bill_id`),
  KEY `idx_payment_reminder_status` (`status`),
  KEY `idx_payment_reminder_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能催缴提醒表';

CREATE TABLE IF NOT EXISTS `property_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(100) DEFAULT NULL,
  `description` VARCHAR(200) DEFAULT NULL,
  `assigned_to` BIGINT DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待处理1已完成',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_property_task_assigned_to` (`assigned_to`),
  KEY `idx_property_task_status` (`status`),
  KEY `idx_property_task_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业任务表';

CREATE TABLE IF NOT EXISTS `repair_urge_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `target_type` VARCHAR(20) NOT NULL COMMENT 'ADMIN/WORKER',
  `target_id` BIGINT DEFAULT NULL,
  `urge_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `reason` VARCHAR(100) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_repair_urge_order_id` (`order_id`),
  KEY `idx_repair_urge_target` (`target_type`,`target_id`),
  KEY `idx_repair_urge_time` (`urge_time`),
  KEY `idx_repair_urge_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单催办日志表';

CREATE TABLE IF NOT EXISTS `user_implicit_profile` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `keywords_json` JSON DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_implicit_profile_user_id` (`user_id`),
  KEY `idx_user_implicit_profile_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户隐式画像表';

CREATE TABLE IF NOT EXISTS `sys_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `content` VARCHAR(500) DEFAULT NULL,
  `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0未读1已读',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_sys_message_user_read` (`user_id`, `is_read`),
  KEY `idx_sys_message_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统消息表';

CREATE TABLE IF NOT EXISTS `system_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `config_key` VARCHAR(100) NOT NULL,
  `config_value` JSON DEFAULT NULL,
  `description` VARCHAR(200) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_system_config_key` (`config_key`),
  KEY `idx_system_config_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置表';

CREATE TABLE IF NOT EXISTS `worker` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL COMMENT '维修员姓名',
  `skill_tags` VARCHAR(200) DEFAULT NULL COMMENT '技能标签，逗号分隔',
  `current_tasks` INT NOT NULL DEFAULT 0 COMMENT '当前工单数',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_worker_enabled` (`enabled`),
  KEY `idx_worker_current_tasks` (`current_tasks`),
  KEY `idx_worker_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='维修员推荐兼容表';

DELIMITER //
CREATE PROCEDURE add_column_if_missing(
  IN p_table VARCHAR(64),
  IN p_column VARCHAR(64),
  IN p_definition TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND COLUMN_NAME = p_column
  ) THEN
    SET @sql := CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_definition);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//

CREATE PROCEDURE add_index_if_missing(
  IN p_table VARCHAR(64),
  IN p_index VARCHAR(64),
  IN p_definition TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND INDEX_NAME = p_index
  ) THEN
    SET @sql := CONCAT('ALTER TABLE `', p_table, '` ADD ', p_definition);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//
DELIMITER ;

CALL add_column_if_missing('complaint', 'sentiment_score', 'DECIMAL(4,2) DEFAULT NULL COMMENT ''情感分数(-5~5)''');
CALL add_column_if_missing('complaint', 'sentiment_label', 'VARCHAR(10) DEFAULT NULL COMMENT ''POSITIVE/NEGATIVE/NEUTRAL''');
CALL add_column_if_missing('complaint', 'risk_level', 'VARCHAR(10) DEFAULT NULL COMMENT ''HIGH/MID/LOW''');
CALL add_column_if_missing('complaint', 'analyzed_at', 'DATETIME DEFAULT NULL COMMENT ''分析时间''');
CALL add_index_if_missing('complaint', 'idx_complaint_sentiment_label', 'KEY `idx_complaint_sentiment_label` (`sentiment_label`)');
CALL add_index_if_missing('complaint', 'idx_complaint_risk_level', 'KEY `idx_complaint_risk_level` (`risk_level`)');
CALL add_index_if_missing('complaint', 'idx_complaint_analyzed_at', 'KEY `idx_complaint_analyzed_at` (`analyzed_at`)');

CALL add_column_if_missing('user', 'credit_score', 'INT DEFAULT 100 COMMENT ''信用分(0~200)''');
CALL add_column_if_missing('user', 'credit_last_update', 'DATETIME DEFAULT NULL COMMENT ''最后更新时间''');
CALL add_column_if_missing('user', 'has_elderly', 'TINYINT(1) DEFAULT 0 COMMENT ''是否有老人''');
CALL add_column_if_missing('user', 'has_child', 'TINYINT(1) DEFAULT 0 COMMENT ''是否有儿童''');
CALL add_column_if_missing('user', 'has_pet', 'TINYINT(1) DEFAULT 0 COMMENT ''是否有宠物''');
CALL add_column_if_missing('user', 'house_area', 'INT DEFAULT NULL COMMENT ''面积(㎡)''');
CALL add_column_if_missing('user', 'room_count', 'TINYINT DEFAULT NULL COMMENT ''房间数''');
CALL add_index_if_missing('user', 'idx_user_credit_score', 'KEY `idx_user_credit_score` (`credit_score`)');

CALL add_column_if_missing('repair_order', 'priority', 'TINYINT DEFAULT NULL COMMENT ''1紧急2普通3低''');
CALL add_column_if_missing('repair_order', 'suggested_worker_id', 'BIGINT DEFAULT NULL COMMENT ''推荐维修员ID''');
CALL add_column_if_missing('repair_order', 'assigned_time', 'DATETIME DEFAULT NULL COMMENT ''派单时间''');
CALL add_column_if_missing('repair_order', 'completion_time', 'DATETIME DEFAULT NULL COMMENT ''完成时间''');
CALL add_column_if_missing('repair_order', 'sla_deadline', 'DATETIME DEFAULT NULL COMMENT ''SLA截止时间''');
CALL add_column_if_missing('repair_order', 'delay_count', 'INT DEFAULT 0 COMMENT ''逾期次数''');
CALL add_index_if_missing('repair_order', 'idx_repair_order_priority', 'KEY `idx_repair_order_priority` (`priority`)');
CALL add_index_if_missing('repair_order', 'idx_repair_order_suggested_worker', 'KEY `idx_repair_order_suggested_worker` (`suggested_worker_id`)');
CALL add_index_if_missing('repair_order', 'idx_repair_order_sla_deadline', 'KEY `idx_repair_order_sla_deadline` (`sla_deadline`)');

CALL add_column_if_missing('repair_urge_log', 'create_time', 'DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('repair_urge_log', 'update_time', 'DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('repair_urge_log', 'is_deleted', 'TINYINT(1) NOT NULL DEFAULT 0');
CALL add_index_if_missing('repair_urge_log', 'idx_repair_urge_create_time', 'KEY `idx_repair_urge_create_time` (`create_time`)');

CALL add_column_if_missing('user_implicit_profile', 'create_time', 'DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('user_implicit_profile', 'is_deleted', 'TINYINT(1) NOT NULL DEFAULT 0');
CALL add_index_if_missing('user_implicit_profile', 'idx_user_implicit_profile_create_time', 'KEY `idx_user_implicit_profile_create_time` (`create_time`)');

CALL add_column_if_missing('sys_message', 'update_time', 'DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('sys_message', 'is_deleted', 'TINYINT(1) NOT NULL DEFAULT 0');
CALL add_index_if_missing('sys_message', 'idx_sys_message_user_read', 'KEY `idx_sys_message_user_read` (`user_id`, `is_read`)');
CALL add_index_if_missing('sys_message', 'idx_sys_message_create_time', 'KEY `idx_sys_message_create_time` (`create_time`)');

CALL add_column_if_missing('worker', 'create_time', 'DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('worker', 'update_time', 'DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('worker', 'is_deleted', 'TINYINT(1) NOT NULL DEFAULT 0');
CALL add_index_if_missing('worker', 'idx_worker_enabled', 'KEY `idx_worker_enabled` (`enabled`)');
CALL add_index_if_missing('worker', 'idx_worker_current_tasks', 'KEY `idx_worker_current_tasks` (`current_tasks`)');
CALL add_index_if_missing('worker', 'idx_worker_create_time', 'KEY `idx_worker_create_time` (`create_time`)');

DROP PROCEDURE add_column_if_missing;
DROP PROCEDURE add_index_if_missing;
