SET NAMES utf8mb4;
USE smart_community;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE complaint;
TRUNCATE TABLE visitor_notify;
TRUNCATE TABLE visitor_blacklist;
TRUNCATE TABLE visitor_invite;
TRUNCATE TABLE access_token;
TRUNCATE TABLE forum_comment;
TRUNCATE TABLE forum_post;
TRUNCATE TABLE lost_found;
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

INSERT INTO `user` (`id`, `openid`, `unionid`, `account`, `password`, `role`, `nickname`, `avatar_url`, `phone`, `points`, `status`, `create_time`, `update_time`, `is_deleted`) VALUES
(1, 'openid_owner_001', 'unionid_001', 'XQYZ123456', '654321', 1, 'Owner-A', NULL, '13800000001', 1000, 1, NOW() - INTERVAL 40 DAY, NOW() - INTERVAL 40 DAY, 0),
(2, 'openid_owner_002', 'unionid_002', 'XQYZ234567', '765432', 1, 'Owner-B', NULL, '13800000002', 500, 1, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 20 DAY, 0),
(3, 'openid_owner_003', 'unionid_003', 'XQYZ345678', '876543', 1, 'Owner-C', NULL, '13800000003', 0, 1, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 0),
(10, 'openid_admin_001', 'unionid_010', 'WTGL456789', '987654', 2, 'Admin-A', NULL, 'ENC_13900000010', 0, 1, NOW() - INTERVAL 100 DAY, NOW() - INTERVAL 100 DAY, 0),
(20, 'openid_worker_001', 'unionid_020', 'JZWX567890', '098765', 3, 'Worker-A', NULL, '13700000020', 0, 1, NOW() - INTERVAL 80 DAY, NOW() - INTERVAL 80 DAY, 0);

INSERT INTO `worker_staffing` (`id`, `worker_id`, `staff_type`, `position`, `shift_group`, `certificates`, `specialties`, `max_daily_orders`, `current_status`, `create_time`, `update_time`, `is_deleted`) VALUES
(1, 20, 2, '维修-水工', 'A', '水工证', '水电维修,水管漏水,下水道堵塞', 8, 1, NOW() - INTERVAL 80 DAY, NOW() - INTERVAL 1 DAY, 0);

INSERT INTO `property` (`id`, `community`, `building`, `unit`, `room`, `owner_name`, `area`, `status`, `create_time`, `update_time`, `is_deleted`) VALUES
(1001, 'Smart Garden', '1', '1', '101', 'Owner-A', 98.50, 4, NOW() - INTERVAL 50 DAY, NOW() - INTERVAL 50 DAY, 0),
(1002, 'Smart Garden', '1', '1', '102', 'Owner-B', 88.20, 4, NOW() - INTERVAL 49 DAY, NOW() - INTERVAL 49 DAY, 0),
(1003, 'Smart Garden', '2', '1', '201', '', 108.30, 3, NOW() - INTERVAL 48 DAY, NOW() - INTERVAL 48 DAY, 0);

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
(13601, 2, 13002, 'Visitor-B', '您的访客【Visitor-B】已进入小区', 0, NOW() - INTERVAL 20 MINUTE, NOW() - INTERVAL 20 MINUTE, 0);

INSERT INTO `complaint` (`id`, `user_id`, `type`, `title`, `content`, `images`, `status`, `reply`, `reply_time`, `satisfaction`, `create_time`, `update_time`, `is_deleted`) VALUES
(14001, 1, 1, 'Night noise', 'Construction noise after 22:00', '[]', 3, 'Construction adjusted to daytime.', NOW() - INTERVAL 2 DAY, 3, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 2 DAY, 0),
(14002, 2, 2, 'Add parcel locker', 'Suggest adding parcel locker near building 2.', '[]', 1, NULL, NULL, NULL, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 0);
