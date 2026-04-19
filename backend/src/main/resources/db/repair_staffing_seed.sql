SET NAMES utf8mb4;
USE smart_community;

-- 家政维修人员初始化（8名固定家政 + 4名维修/外包）
-- staff_type: 1固定家政 2水工 3电工 4家电维修 5外包合作
INSERT INTO `user` (`id`, `openid`, `unionid`, `account`, `password`, `role`, `nickname`, `avatar_url`, `phone`, `points`, `status`, `create_time`, `update_time`, `is_deleted`) VALUES
(20, 'openid_worker_001', 'unionid_020', 'JZWX567890', '098765', 3, 'HK-Clean-01', NULL, '13700000020', 0, 1, NOW(), NOW(), 0),
(21, 'openid_worker_002', 'unionid_021', 'JZWX567891', '198765', 3, 'HK-Clean-02', NULL, '13700000021', 0, 1, NOW(), NOW(), 0),
(22, 'openid_worker_003', 'unionid_022', 'JZWX567892', '298765', 3, 'HK-Clean-03', NULL, '13700000022', 0, 1, NOW(), NOW(), 0),
(23, 'openid_worker_004', 'unionid_023', 'JZWX567893', '398765', 3, 'HK-Clean-04', NULL, '13700000023', 0, 1, NOW(), NOW(), 0),
(24, 'openid_worker_005', 'unionid_024', 'JZWX567894', '498765', 3, 'HK-Clean-05', NULL, '13700000024', 0, 1, NOW(), NOW(), 0),
(25, 'openid_worker_006', 'unionid_025', 'JZWX567895', '598765', 3, 'HK-Clean-06', NULL, '13700000025', 0, 1, NOW(), NOW(), 0),
(26, 'openid_worker_007', 'unionid_026', 'JZWX567896', '698765', 3, 'HK-Clean-07', NULL, '13700000026', 0, 1, NOW(), NOW(), 0),
(27, 'openid_worker_008', 'unionid_027', 'JZWX567897', '798765', 3, 'HK-Clean-08', NULL, '13700000027', 0, 1, NOW(), NOW(), 0),
(28, 'openid_worker_009', 'unionid_028', 'JZWX567898', '898765', 3, 'Repair-Plumber-01', NULL, '13700000028', 0, 1, NOW(), NOW(), 0),
(29, 'openid_worker_010', 'unionid_029', 'JZWX567899', '998765', 3, 'Repair-Electric-01', NULL, '13700000029', 0, 1, NOW(), NOW(), 0),
(30, 'openid_worker_011', 'unionid_030', 'JZWX567900', '009765', 3, 'Repair-Appliance-01', NULL, '13700000030', 0, 1, NOW(), NOW(), 0),
(31, 'openid_worker_012', 'unionid_031', 'JZWX567901', '109765', 3, 'Repair-Outsource-01', NULL, '13700000031', 0, 1, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
`openid` = VALUES(`openid`),
`unionid` = VALUES(`unionid`),
`account` = VALUES(`account`),
`password` = VALUES(`password`),
`role` = 3,
`nickname` = VALUES(`nickname`),
`phone` = VALUES(`phone`),
`status` = 1,
`is_deleted` = 0,
`update_time` = NOW();

INSERT INTO `worker_staffing`
(`worker_id`, `staff_type`, `position`, `shift_group`, `certificates`, `specialties`, `max_daily_orders`, `current_status`, `create_time`, `update_time`, `is_deleted`)
VALUES
(20, 1, 'housekeeping-fixed', 'A', '家政服务证,housekeeping_cert', '日常保洁,全屋大扫除,厨房深度清洁,卫生间消毒,housekeeping,cleaning', 6, 1, NOW(), NOW(), 0),
(21, 1, 'housekeeping-fixed', 'A', '家政服务证,housekeeping_cert', '日常保洁,玻璃擦拭,地板打蜡,沙发地毯清洗,housekeeping,cleaning', 6, 1, NOW(), NOW(), 0),
(22, 1, 'housekeeping-fixed', 'B', '家政服务证,cleaning_cert,appliance_clean_cert', '家电清洗,空调清洗,油烟机清洗,洗衣机清洗,cleaning,appliance', 6, 1, NOW(), NOW(), 0),
(23, 1, 'housekeeping-fixed', 'B', '家政服务证,cleaning_cert,appliance_clean_cert', '家电清洗,冰箱清洗,热水器除垢,净水器故障,cleaning,appliance', 6, 1, NOW(), NOW(), 0),
(24, 1, 'housekeeping-fixed', 'C', '养老护理证,caregiver_cert', '养老护理,老人陪护,助浴,康复按摩,care,nurse', 5, 1, NOW(), NOW(), 0),
(25, 1, 'housekeeping-fixed', 'C', '家政服务证,专项服务证,special_service_cert', '专项服务,除螨服务,开荒保洁,家庭收纳整理,special,cleaning', 5, 1, NOW(), NOW(), 0),
(26, 1, 'housekeeping-fixed', 'A', '家政服务证,housekeeping_cert', '日常保洁,宠物护理,开荒保洁,housekeeping,cleaning', 6, 1, NOW(), NOW(), 0),
(27, 1, 'housekeeping-fixed', 'B', '家政服务证,cleaning_cert', '日常保洁,家电清洗,玻璃擦拭,地板打蜡,cleaning', 6, 1, NOW(), NOW(), 0),
(28, 2, 'repair-plumber', 'A', '水工证,plumber_cert', '水电维修,水管漏水,马桶疏通,下水道堵塞,热水器故障,plumber,water,drain', 8, 1, NOW(), NOW(), 0),
(29, 3, 'repair-electrician', 'A', '电工证,electrician_cert', '水电维修,电路跳闸,插座损坏,灯具维修,线路老化更换,electric,power', 8, 1, NOW(), NOW(), 0),
(30, 4, 'repair-appliance', 'B', '家电维修证,appliance_cert', '家电维修,空调不制冷,冰箱不制冷,洗衣机不转,油烟机故障,appliance', 8, 1, NOW(), NOW(), 0),
(31, 5, 'repair-outsource', 'OUT', '外包资质,outsource_company_qualification', '房屋结构,家具维修,智能设备,其他,专项服务,outsource,cooperation', 15, 1, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
`staff_type` = VALUES(`staff_type`),
`position` = VALUES(`position`),
`shift_group` = VALUES(`shift_group`),
`certificates` = VALUES(`certificates`),
`specialties` = VALUES(`specialties`),
`max_daily_orders` = VALUES(`max_daily_orders`),
`current_status` = VALUES(`current_status`),
`is_deleted` = 0,
`update_time` = NOW();

