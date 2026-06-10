INSERT INTO `facility_info` (
  `name`, `category`, `location`, `open_hours`, `contact_phone`, `status`, `sort_order`,
  `description`, `image_urls`, `last_inspection_time`, `create_time`, `update_time`, `is_deleted`
)
SELECT '中心花园儿童活动区', '儿童设施', '一期中心花园东侧', '08:00-21:00', '0418-10086', 1, 10,
       '含滑梯、秋千和缓冲地垫，雨后地垫湿滑时请注意安全。', NULL, NOW() - INTERVAL 1 DAY, NOW(), NOW(), 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `facility_info` WHERE `is_deleted` = 0 AND `name` = '中心花园儿童活动区'
);

INSERT INTO `facility_info` (
  `name`, `category`, `location`, `open_hours`, `contact_phone`, `status`, `sort_order`,
  `description`, `image_urls`, `last_inspection_time`, `create_time`, `update_time`, `is_deleted`
)
SELECT 'B2 西区充电桩', '停车设施', 'B2 地下停车场西区', '00:00-24:00', '0418-10010', 2, 20,
       '当前处于维护中，预计本周内完成线路复检并恢复开放。', NULL, NOW() - INTERVAL 3 HOUR, NOW(), NOW(), 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `facility_info` WHERE `is_deleted` = 0 AND `name` = 'B2 西区充电桩'
);

INSERT INTO `facility_info` (
  `name`, `category`, `location`, `open_hours`, `contact_phone`, `status`, `sort_order`,
  `description`, `image_urls`, `last_inspection_time`, `create_time`, `update_time`, `is_deleted`
)
SELECT '社区共享会议室', '便民设施', '物业服务中心二层', '09:00-20:00', '0418-10000', 1, 30,
       '可用于小型居民议事、志愿活动和亲子课堂预约。', NULL, NOW() - INTERVAL 6 HOUR, NOW(), NOW(), 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `facility_info` WHERE `is_deleted` = 0 AND `name` = '社区共享会议室'
);

INSERT INTO `express_package` (
  `property_id`, `courier_company`, `tracking_no`, `pickup_code`, `recipient_name`, `recipient_phone`,
  `shelf_location`, `status`, `arrived_time`, `pickup_time`, `remark`, `create_time`, `update_time`, `is_deleted`
)
SELECT p.`id`, '顺丰速运', CONCAT('SF', DATE_FORMAT(NOW(), '%y%m%d'), LPAD(p.`id`, 6, '0')), 'A1286',
       p.`owner_name`, NULL, '北门快递架 A 区', 0, NOW() - INTERVAL 2 HOUR, NULL,
       '冷链生鲜请尽快领取。', NOW(), NOW(), 0
FROM `property` p
WHERE p.`is_deleted` = 0
  AND p.`status` IN (4, 5)
  AND NOT EXISTS (
    SELECT 1
    FROM `express_package` e
    WHERE e.`is_deleted` = 0
      AND e.`property_id` = p.`id`
      AND e.`courier_company` = '顺丰速运'
      AND e.`status` = 0
  )
ORDER BY p.`id`
LIMIT 1;

INSERT INTO `express_package` (
  `property_id`, `courier_company`, `tracking_no`, `pickup_code`, `recipient_name`, `recipient_phone`,
  `shelf_location`, `status`, `arrived_time`, `pickup_time`, `remark`, `create_time`, `update_time`, `is_deleted`
)
SELECT p.`id`, '京东物流', CONCAT('JD', DATE_FORMAT(NOW(), '%y%m%d'), LPAD(p.`id`, 6, '0')), 'B3042',
       p.`owner_name`, NULL, '北门快递架 B 区', 1, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 8 HOUR,
       '已由业主签收。', NOW(), NOW(), 0
FROM `property` p
WHERE p.`is_deleted` = 0
  AND p.`status` IN (4, 5)
  AND NOT EXISTS (
    SELECT 1
    FROM `express_package` e
    WHERE e.`is_deleted` = 0
      AND e.`property_id` = p.`id`
      AND e.`courier_company` = '京东物流'
  )
ORDER BY p.`id`
LIMIT 1 OFFSET 1;
