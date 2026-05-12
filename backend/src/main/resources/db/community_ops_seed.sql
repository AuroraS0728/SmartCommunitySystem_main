SET NAMES utf8mb4;
USE smart_community;

UPDATE user
SET has_elderly = 1,
    has_child = 0,
    has_pet = 0,
    house_area = 138,
    room_count = 4,
    credit_score = 96,
    credit_last_update = NOW(),
    update_time = NOW()
WHERE id = 1 AND is_deleted = 0;

UPDATE user
SET has_elderly = 0,
    has_child = 1,
    has_pet = 0,
    house_area = 108,
    room_count = 3,
    credit_score = 102,
    credit_last_update = NOW(),
    update_time = NOW()
WHERE id = 2 AND is_deleted = 0;

UPDATE user
SET has_elderly = 1,
    has_child = 1,
    has_pet = 0,
    house_area = 126,
    room_count = 4,
    credit_score = 98,
    credit_last_update = NOW(),
    update_time = NOW()
WHERE id = 39 AND is_deleted = 0;

UPDATE user
SET has_elderly = 0,
    has_child = 1,
    has_pet = 0,
    house_area = 96,
    room_count = 3,
    credit_score = 100,
    credit_last_update = NOW(),
    update_time = NOW()
WHERE id = 40 AND is_deleted = 0;

UPDATE user
SET has_elderly = 0,
    has_child = 0,
    has_pet = 1,
    house_area = 88,
    room_count = 2,
    credit_score = 97,
    credit_last_update = NOW(),
    update_time = NOW()
WHERE id = 42 AND is_deleted = 0;

UPDATE user
SET has_elderly = 0,
    has_child = 0,
    has_pet = 1,
    house_area = 92,
    room_count = 2,
    credit_score = 95,
    credit_last_update = NOW(),
    update_time = NOW()
WHERE id = 43 AND is_deleted = 0;

UPDATE user
SET has_elderly = 1,
    has_child = 0,
    has_pet = 0,
    house_area = 82,
    room_count = 2,
    credit_score = 99,
    credit_last_update = NOW(),
    update_time = NOW()
WHERE id = 48 AND is_deleted = 0;

UPDATE user
SET has_elderly = 1,
    has_child = 0,
    has_pet = 0,
    house_area = 85,
    room_count = 2,
    credit_score = 94,
    credit_last_update = NOW(),
    update_time = NOW()
WHERE id = 49 AND is_deleted = 0;

INSERT INTO notice (title, content, publisher, top, attachment_urls, publish_time, create_time, update_time, is_deleted)
SELECT
  '5月园区绿化养护与喷灌安排',
  '物业绿化组将于本周四、周五上午 8:30 至 11:00 对一期中心花园与环湖步道沿线绿化带进行修剪、施肥和喷灌。请住户临时避让作业区域，勿将车辆停放在绿化养护临停位。',
  '智慧社区物业服务中心',
  1,
  NULL,
  NOW() - INTERVAL 2 DAY,
  NOW(),
  NOW(),
  0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM notice WHERE is_deleted = 0 AND title = '5月园区绿化养护与喷灌安排');

INSERT INTO notice (title, content, publisher, top, attachment_urls, publish_time, create_time, update_time, is_deleted)
SELECT
  '地库西区照明检修完成通告',
  'B2 西区照明线路检修已于今日 16:30 完成，夜间照度恢复正常。若仍发现局部灯带不亮，请通过小程序报修并注明车位编号，物业将在 2 小时内复核。',
  '智慧社区工程部',
  0,
  NULL,
  NOW() - INTERVAL 1 DAY,
  NOW(),
  NOW(),
  0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM notice WHERE is_deleted = 0 AND title = '地库西区照明检修完成通告');

INSERT INTO notice (title, content, publisher, top, attachment_urls, publish_time, create_time, update_time, is_deleted)
SELECT
  '儿童游乐区安全垫局部更换提醒',
  '因南区儿童游乐区缓冲地垫老化，物业将于本周六 9:00-12:00 进行局部更换。施工期间该区域暂停开放，请家长朋友注意绕行并看护儿童。',
  '智慧社区客服前台',
  0,
  NULL,
  NOW(),
  NOW(),
  NOW(),
  0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM notice WHERE is_deleted = 0 AND title = '儿童游乐区安全垫局部更换提醒');

INSERT INTO second_hand (
  user_id, community, title, category, description, price, images, status, view_count, report_count, contact, create_time, update_time, is_deleted
)
SELECT
  39, 'Smart Garden', '九成新儿童书桌椅组合', 'furniture',
  '书桌 1.2 米，带抽屉和可调节椅子，适合小学阶段孩子使用。家里整理空间，支持同小区自提。',
  320.00,
  '["https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=900&q=80"]',
  1, 58, 0, '13000001443', NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 2 DAY, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM second_hand WHERE is_deleted = 0 AND title = '九成新儿童书桌椅组合');

INSERT INTO second_hand (
  user_id, community, title, category, description, price, images, status, view_count, report_count, contact, create_time, update_time, is_deleted
)
SELECT
  42, 'Smart Garden', '宠物航空箱 M 号', 'daily',
  '猫狗通用航空箱，带可拆洗垫布和透气侧窗，社区内可送到北门自提点。',
  90.00,
  '["https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?auto=format&fit=crop&w=900&q=80"]',
  1, 37, 0, '13000001554', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 1 DAY, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM second_hand WHERE is_deleted = 0 AND title = '宠物航空箱 M 号');

INSERT INTO second_hand (
  user_id, community, title, category, description, price, images, status, view_count, report_count, contact, create_time, update_time, is_deleted
)
SELECT
  45, 'Smart Garden', '露营折叠天幕+地钉一套', 'other',
  '去年秋天活动买的，使用过 2 次，无破损，适合草坪露营和周末野餐。',
  260.00,
  '["https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=900&q=80"]',
  1, 44, 0, '13000001665', NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 1 DAY, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM second_hand WHERE is_deleted = 0 AND title = '露营折叠天幕+地钉一套');

INSERT INTO second_hand (
  user_id, community, title, category, description, price, images, status, view_count, report_count, contact, create_time, update_time, is_deleted
)
SELECT
  48, 'Smart Garden', '老人助行器 可折叠带座', 'other',
  '家里备用助行器，带坐凳和刹车，轮子顺滑，适合短距离外出使用。',
  180.00,
  '["https://images.unsplash.com/photo-1584515933487-779824d29309?auto=format&fit=crop&w=900&q=80"]',
  1, 29, 0, '13000001776', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 1 DAY, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM second_hand WHERE is_deleted = 0 AND title = '老人助行器 可折叠带座');

INSERT INTO second_hand (
  user_id, community, title, category, description, price, images, status, view_count, report_count, contact, create_time, update_time, is_deleted
)
SELECT
  50, 'Smart Garden', '95新跑步手环 支持心率监测', 'digital',
  '功能正常，续航 5-6 天，换新出手，适合夜跑和日常计步。',
  150.00,
  '["https://images.unsplash.com/photo-1510017803434-a899398421b3?auto=format&fit=crop&w=900&q=80"]',
  1, 33, 0, '13000001850', NOW() - INTERVAL 2 DAY, NOW(), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM second_hand WHERE is_deleted = 0 AND title = '95新跑步手环 支持心率监测');

INSERT INTO second_hand_favorite (user_id, second_hand_id, create_time)
SELECT 1, s.id, NOW()
FROM second_hand s
WHERE s.title = '九成新儿童书桌椅组合'
  AND NOT EXISTS (SELECT 1 FROM second_hand_favorite f WHERE f.user_id = 1 AND f.second_hand_id = s.id);

INSERT INTO second_hand_favorite (user_id, second_hand_id, create_time)
SELECT 2, s.id, NOW()
FROM second_hand s
WHERE s.title = '露营折叠天幕+地钉一套'
  AND NOT EXISTS (SELECT 1 FROM second_hand_favorite f WHERE f.user_id = 2 AND f.second_hand_id = s.id);

INSERT INTO second_hand_favorite (user_id, second_hand_id, create_time)
SELECT 39, s.id, NOW()
FROM second_hand s
WHERE s.title = '宠物航空箱 M 号'
  AND NOT EXISTS (SELECT 1 FROM second_hand_favorite f WHERE f.user_id = 39 AND f.second_hand_id = s.id);

INSERT INTO second_hand_favorite (user_id, second_hand_id, create_time)
SELECT 42, s.id, NOW()
FROM second_hand s
WHERE s.title = '95新跑步手环 支持心率监测'
  AND NOT EXISTS (SELECT 1 FROM second_hand_favorite f WHERE f.user_id = 42 AND f.second_hand_id = s.id);

INSERT INTO lost_found (
  user_id, type, title, description, location, contact, status, images, create_time, update_time, is_deleted
)
SELECT
  41, 1, '在2号楼门厅拾到一串门禁卡钥匙', '今早 8 点左右在 2号楼门厅沙发旁捡到一串黑色门禁卡钥匙，含两把银色钥匙和一个蓝色挂饰。',
  '2号楼一层门厅', '13000001517', 1,
  '["https://images.unsplash.com/photo-1516549655169-df83a0774514?auto=format&fit=crop&w=900&q=80"]',
  NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM lost_found WHERE is_deleted = 0 AND title = '在2号楼门厅拾到一串门禁卡钥匙');

INSERT INTO lost_found (
  user_id, type, title, description, location, contact, status, images, create_time, update_time, is_deleted
)
SELECT
  44, 2, '寻找一只粉色儿童水杯', '孩子在南区游乐场丢失一只粉色保温水杯，杯身有卡通兔贴纸，如有捡到烦请联系。',
  '南区游乐场附近', '13000001628', 1,
  '["https://images.unsplash.com/photo-1514228742587-6b1558fcf93a?auto=format&fit=crop&w=900&q=80"]',
  NOW() - INTERVAL 1 DAY, NOW(), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM lost_found WHERE is_deleted = 0 AND title = '寻找一只粉色儿童水杯');

INSERT INTO lost_found (
  user_id, type, title, description, location, contact, status, images, create_time, update_time, is_deleted
)
SELECT
  52, 1, '环湖步道长椅上拾到运动蓝牙耳机', '夜跑结束后在环湖步道北段长椅上捡到一副白色蓝牙耳机，已交物业前台保管。',
  '环湖步道北段长椅', '13000001924', 1,
  '["https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=80"]',
  NOW() - INTERVAL 6 HOUR, NOW(), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM lost_found WHERE is_deleted = 0 AND title = '环湖步道长椅上拾到运动蓝牙耳机');

INSERT INTO forum_post (
  user_id, board, title, content, view_count, like_cnt, reply_cnt, is_top, is_essence, create_time, update_time, is_deleted
)
SELECT
  39, 'chat', '本周末亲子露营活动有人一起准备装备吗？',
  '报名了周日草坪露营体验营，想和邻居们一起拼一个野餐垫和露营灯，有经验的住户可以分享一下给小朋友准备什么会更合适。',
  96, 12, 2, 0, 1, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 1 DAY, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM forum_post WHERE is_deleted = 0 AND title = '本周末亲子露营活动有人一起准备装备吗？');

INSERT INTO forum_post (
  user_id, board, title, content, view_count, like_cnt, reply_cnt, is_top, is_essence, create_time, update_time, is_deleted
)
SELECT
  42, 'chat', '宠物友好社交日建议带什么用品？',
  '第一次参加社区宠物活动，家里是柯基，除了牵引绳和饮水碗，还需要准备什么？现场会有简单清洁区吗？',
  88, 9, 2, 0, 0, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM forum_post WHERE is_deleted = 0 AND title = '宠物友好社交日建议带什么用品？');

INSERT INTO forum_post (
  user_id, board, title, content, view_count, like_cnt, reply_cnt, is_top, is_essence, create_time, update_time, is_deleted
)
SELECT
  48, 'chat', '给长辈办智能手机课堂很有必要',
  '家里老人最近学会了线上缴费和门禁邀请码，少跑很多趟物业前台。建议以后每月都固定开一场长者数字课堂。',
  74, 16, 1, 1, 1, NOW() - INTERVAL 1 DAY, NOW(), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM forum_post WHERE is_deleted = 0 AND title = '给长辈办智能手机课堂很有必要');

INSERT INTO forum_comment (post_id, user_id, parent_id, content, like_cnt, create_time, update_time, is_deleted)
SELECT p.id, 40, NULL, '我们家也报名了，可以一起拼防潮垫，孩子 5 岁。', 3, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY, 0
FROM forum_post p
WHERE p.title = '本周末亲子露营活动有人一起准备装备吗？'
  AND NOT EXISTS (SELECT 1 FROM forum_comment c WHERE c.post_id = p.id AND c.user_id = 40 AND c.content = '我们家也报名了，可以一起拼防潮垫，孩子 5 岁。');

INSERT INTO forum_comment (post_id, user_id, parent_id, content, like_cnt, create_time, update_time, is_deleted)
SELECT p.id, 45, NULL, '建议带一套轻便换洗衣服，晚上草坪上孩子容易出汗。', 2, NOW() - INTERVAL 30 HOUR, NOW() - INTERVAL 1 DAY, 0
FROM forum_post p
WHERE p.title = '本周末亲子露营活动有人一起准备装备吗？'
  AND NOT EXISTS (SELECT 1 FROM forum_comment c WHERE c.post_id = p.id AND c.user_id = 45 AND c.content = '建议带一套轻便换洗衣服，晚上草坪上孩子容易出汗。');

INSERT INTO forum_comment (post_id, user_id, parent_id, content, like_cnt, create_time, update_time, is_deleted)
SELECT p.id, 43, NULL, '最好带便携湿巾和拾便袋，现场应该也会准备一点基础物资。', 4, NOW() - INTERVAL 28 HOUR, NOW() - INTERVAL 20 HOUR, 0
FROM forum_post p
WHERE p.title = '宠物友好社交日建议带什么用品？'
  AND NOT EXISTS (SELECT 1 FROM forum_comment c WHERE c.post_id = p.id AND c.user_id = 43 AND c.content = '最好带便携湿巾和拾便袋，现场应该也会准备一点基础物资。');

INSERT INTO forum_comment (post_id, user_id, parent_id, content, like_cnt, create_time, update_time, is_deleted)
SELECT p.id, 44, NULL, '上次物业说会准备简易清洁点和饮水桶，可以放心参加。', 2, NOW() - INTERVAL 24 HOUR, NOW() - INTERVAL 18 HOUR, 0
FROM forum_post p
WHERE p.title = '宠物友好社交日建议带什么用品？'
  AND NOT EXISTS (SELECT 1 FROM forum_comment c WHERE c.post_id = p.id AND c.user_id = 44 AND c.content = '上次物业说会准备简易清洁点和饮水桶，可以放心参加。');

INSERT INTO forum_comment (post_id, user_id, parent_id, content, like_cnt, create_time, update_time, is_deleted)
SELECT p.id, 49, NULL, '给老年人一步一步讲操作确实很有帮助，希望以后还能有反诈专题。', 5, NOW() - INTERVAL 12 HOUR, NOW() - INTERVAL 8 HOUR, 0
FROM forum_post p
WHERE p.title = '给长辈办智能手机课堂很有必要'
  AND NOT EXISTS (SELECT 1 FROM forum_comment c WHERE c.post_id = p.id AND c.user_id = 49 AND c.content = '给老年人一步一步讲操作确实很有帮助，希望以后还能有反诈专题。');

UPDATE forum_post p
JOIN (
  SELECT post_id, COUNT(*) AS reply_cnt
  FROM forum_comment
  WHERE is_deleted = 0
  GROUP BY post_id
) c ON c.post_id = p.id
SET p.reply_cnt = c.reply_cnt,
    p.update_time = NOW()
WHERE p.is_deleted = 0;

INSERT INTO complaint (
  user_id, type, title, content, images, status, reply, reply_time, satisfaction, create_time, update_time, is_deleted, sentiment_score, sentiment_label, risk_level, analyzed_at
)
SELECT
  43, 2, '北门快递架晚高峰取件拥堵', '北门快递架晚上 7 点后取件的人比较多，照明也有点暗，建议增加临时引导和一盏补光灯，老人取件不太方便。',
  NULL, 2,
  '已安排客服与工程部联合查看，今晚先加装临时补光灯，本周内优化取件指引和通道动线。',
  NOW() - INTERVAL 8 HOUR,
  5,
  NOW() - INTERVAL 2 DAY,
  NOW(),
  0,
  0.78,
  '中性',
  '中',
  NOW() - INTERVAL 1 DAY
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM complaint WHERE is_deleted = 0 AND title = '北门快递架晚高峰取件拥堵');

INSERT INTO complaint (
  user_id, type, title, content, images, status, reply, reply_time, satisfaction, create_time, update_time, is_deleted, sentiment_score, sentiment_label, risk_level, analyzed_at
)
SELECT
  49, 1, '2号楼电梯午间异响希望尽快排查', '中午 12 点半左右 2号楼东侧电梯上行时有持续异响，家里老人一个人乘坐会有点紧张，希望尽快排查一下。',
  NULL, 1,
  NULL,
  NULL,
  NULL,
  NOW() - INTERVAL 10 HOUR,
  NOW() - INTERVAL 10 HOUR,
  0,
  -0.63,
  '负面',
  '高',
  NOW() - INTERVAL 9 HOUR
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM complaint WHERE is_deleted = 0 AND title = '2号楼电梯午间异响希望尽快排查');
