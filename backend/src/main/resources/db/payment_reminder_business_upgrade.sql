ALTER TABLE `payment_reminder`
  ADD COLUMN IF NOT EXISTS `business_type` TINYINT NOT NULL DEFAULT 1 COMMENT '1物业费 2停车费 3维修费' AFTER `fee_bill_id`,
  ADD COLUMN IF NOT EXISTS `business_id` BIGINT DEFAULT NULL COMMENT '对应费用业务ID' AFTER `business_type`;

UPDATE `payment_reminder`
SET `business_type` = 1,
    `business_id` = `fee_bill_id`
WHERE `business_id` IS NULL;

CREATE INDEX IF NOT EXISTS `idx_payment_reminder_business`
  ON `payment_reminder` (`business_type`, `business_id`);
