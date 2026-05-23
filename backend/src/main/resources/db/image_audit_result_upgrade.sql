CREATE TABLE IF NOT EXISTS `image_audit_result` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `trade_id` BIGINT NOT NULL,
  `image_url` VARCHAR(500) NOT NULL,
  `detect_label` VARCHAR(32) DEFAULT NULL,
  `real_probability` DECIMAL(10,6) DEFAULT NULL,
  `fake_probability` DECIMAL(10,6) DEFAULT NULL,
  `threshold_value` DECIMAL(10,6) DEFAULT NULL,
  `risk_level` VARCHAR(32) DEFAULT NULL,
  `audit_status` VARCHAR(32) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_image_audit_trade` (`trade_id`),
  KEY `idx_image_audit_status` (`audit_status`),
  KEY `idx_image_audit_risk` (`risk_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
