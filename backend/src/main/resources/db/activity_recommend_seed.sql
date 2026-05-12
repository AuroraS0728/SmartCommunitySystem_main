SET NAMES utf8mb4;
USE smart_community;

INSERT INTO recommend_rule (
  rule_name, service_id, service_name, rule_expression, condition_json, image_url, price, priority, enabled, create_time, update_time, is_deleted
)
SELECT
  '长者居家关怀推荐',
  'aged-friendly-upgrade',
  '适老化改造',
  'has_elderly == true',
  '{"reason":"检测到家中有老人住户，优先推荐防滑扶手、夜间照明和紧急呼叫改造服务","popupEnabled":true,"keywordsAny":["老人","轮椅","高龄","防滑"],"actionPath":"/pages/service/service"}',
  'https://images.unsplash.com/photo-1516574187841-cb9cc2ca948b?auto=format&fit=crop&w=900&q=80',
  2999,
  1,
  1,
  NOW(),
  NOW(),
  0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM recommend_rule WHERE is_deleted = 0 AND service_id = 'aged-friendly-upgrade'
);

INSERT INTO recommend_rule (
  rule_name, service_id, service_name, rule_expression, condition_json, image_url, price, priority, enabled, create_time, update_time, is_deleted
)
SELECT
  '水电高频报修推荐',
  'water-electric-maintenance',
  '水电保养套餐',
  'water_electric_repair_count >= 2',
  '{"reason":"近阶段水电或家电报修较频繁，建议集中做一次上门巡检和保养","popupEnabled":true,"minRepairCount":2,"actionPath":"/pages/repair/list"}',
  'https://images.unsplash.com/photo-1581578731548-c64695cc6952?auto=format&fit=crop&w=900&q=80',
  399,
  2,
  1,
  NOW(),
  NOW(),
  0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM recommend_rule WHERE is_deleted = 0 AND service_id = 'water-electric-maintenance'
);

INSERT INTO recommend_rule (
  rule_name, service_id, service_name, rule_expression, condition_json, image_url, price, priority, enabled, create_time, update_time, is_deleted
)
SELECT
  '大户型保洁推荐',
  'deep-cleaning',
  '深度保洁',
  'house_area >= 120',
  '{"reason":"大户型住户更适合季度深度保洁和软装除尘服务","popupEnabled":false,"minHouseArea":120,"actionPath":"/pages/service/service"}',
  'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=900&q=80',
  599,
  3,
  1,
  NOW(),
  NOW(),
  0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM recommend_rule WHERE is_deleted = 0 AND service_id = 'deep-cleaning'
);

INSERT INTO recommend_rule (
  rule_name, service_id, service_name, rule_expression, condition_json, image_url, price, priority, enabled, create_time, update_time, is_deleted
)
SELECT
  '宠物家庭空气焕新推荐',
  'pet-mite-cleaning',
  '宠物除螨服务',
  'has_pet == true',
  '{"reason":"宠物家庭建议定期做除螨和异味治理，提升公共环境与居家舒适度","popupEnabled":true,"keywordsAny":["宠物","猫","狗"],"actionPath":"/pages/service/service"}',
  'https://images.unsplash.com/photo-1517849845537-4d257902454a?auto=format&fit=crop&w=900&q=80',
  199,
  4,
  1,
  NOW(),
  NOW(),
  0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM recommend_rule WHERE is_deleted = 0 AND service_id = 'pet-mite-cleaning'
);

INSERT INTO recommend_rule (
  rule_name, service_id, service_name, rule_expression, condition_json, image_url, price, priority, enabled, create_time, update_time, is_deleted
)
SELECT
  '亲子家庭收纳焕新推荐',
  'family-storage-refresh',
  '亲子收纳焕新包',
  'has_child == true',
  '{"reason":"亲子家庭日常物品较多，推荐儿童房收纳优化与安全整理服务","popupEnabled":true,"actionPath":"/pages/service/service"}',
  'https://images.unsplash.com/photo-1516627145497-ae6968895b74?auto=format&fit=crop&w=900&q=80',
  699,
  5,
  1,
  NOW(),
  NOW(),
  0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM recommend_rule WHERE is_deleted = 0 AND service_id = 'family-storage-refresh'
);

INSERT INTO recommend_rule (
  rule_name, service_id, service_name, rule_expression, condition_json, image_url, price, priority, enabled, create_time, update_time, is_deleted
)
SELECT
  '夏季空调焕新推荐',
  'summer-ac-maintenance',
  '空调清洗保养',
  'house_area >= 80',
  '{"reason":"即将进入夏季用冷高峰，建议提前安排空调清洗与滤网消杀","popupEnabled":false,"actionPath":"/pages/service/service"}',
  'https://images.unsplash.com/photo-1621905252507-b35492cc74b4?auto=format&fit=crop&w=900&q=80',
  269,
  6,
  1,
  NOW(),
  NOW(),
  0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM recommend_rule WHERE is_deleted = 0 AND service_id = 'summer-ac-maintenance'
);

INSERT INTO activity (
  title, description, type, image_url, start_time, end_time, location,
  max_participants, current_participants, age_limit, with_child_required, with_pet_required,
  status, create_time, update_time, is_deleted
)
SELECT
  '周末便民义诊与血压筛查',
  '联合社区卫生服务站开展便民义诊，提供血压血糖检测、慢病咨询与长者用药提醒，现场还有家庭医生签约咨询台。',
  '公益',
  'https://images.unsplash.com/photo-1584515933487-779824d29309?auto=format&fit=crop&w=1200&q=80',
  '2026-05-16 09:00:00',
  '2026-05-16 11:30:00',
  '一期中心广场便民服务棚',
  60,
  0,
  '>=18',
  0,
  0,
  0,
  NOW(),
  NOW(),
  0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM activity WHERE is_deleted = 0 AND title = '周末便民义诊与血压筛查'
);

INSERT INTO activity (
  title, description, type, image_url, start_time, end_time, location,
  max_participants, current_participants, age_limit, with_child_required, with_pet_required,
  status, create_time, update_time, is_deleted
)
SELECT
  '春末山野徒步打卡日',
  '物业与业委会联合组织轻量徒步，往返约 6 公里，含补给包、打卡摄影与安全引导，适合想认识邻里的年轻住户。',
  '爬山',
  'https://images.unsplash.com/photo-1551632811-561732d1e306?auto=format&fit=crop&w=1200&q=80',
  '2026-05-18 08:00:00',
  '2026-05-18 15:00:00',
  '社区东门集合前往凤凰山步道',
  28,
  0,
  '18-50',
  0,
  0,
  0,
  NOW(),
  NOW(),
  0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM activity WHERE is_deleted = 0 AND title = '春末山野徒步打卡日'
);

INSERT INTO activity (
  title, description, type, image_url, start_time, end_time, location,
  max_participants, current_participants, age_limit, with_child_required, with_pet_required,
  status, create_time, update_time, is_deleted
)
SELECT
  '亲子草坪露营体验营',
  '搭建轻露营体验区，安排绘本分享、泡泡互动、野餐草坪游戏和家庭合影打卡，面向社区亲子家庭开放。',
  '露营',
  'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80',
  '2026-05-24 15:00:00',
  '2026-05-24 19:00:00',
  '南区中央草坪',
  18,
  0,
  '3-12',
  1,
  0,
  0,
  NOW(),
  NOW(),
  0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM activity WHERE is_deleted = 0 AND title = '亲子草坪露营体验营'
);

INSERT INTO activity (
  title, description, type, image_url, start_time, end_time, location,
  max_participants, current_participants, age_limit, with_child_required, with_pet_required,
  status, create_time, update_time, is_deleted
)
SELECT
  '萌宠友好社交日',
  '邀请养宠住户在宠物友好区交流养护经验，设置宠物义诊、牵引礼仪讲解和宠物摄影打卡位，宠物不计入人数。',
  '宠物',
  'https://images.unsplash.com/photo-1548199973-03cce0bbc87b?auto=format&fit=crop&w=1200&q=80',
  '2026-05-26 16:00:00',
  '2026-05-26 18:30:00',
  '北门宠物友好活动区',
  24,
  0,
  '>=16',
  0,
  1,
  0,
  NOW(),
  NOW(),
  0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM activity WHERE is_deleted = 0 AND title = '萌宠友好社交日'
);

INSERT INTO activity (
  title, description, type, image_url, start_time, end_time, location,
  max_participants, current_participants, age_limit, with_child_required, with_pet_required,
  status, create_time, update_time, is_deleted
)
SELECT
  '六一亲子手作派对',
  '围绕六一主题开展亲子烘焙、手工相框和社区寻宝游戏，适合家长与儿童共同参与，现场提供饮用水和简餐。',
  '亲子',
  'https://images.unsplash.com/photo-1516627145497-ae6968895b74?auto=format&fit=crop&w=1200&q=80',
  '2026-06-01 14:00:00',
  '2026-06-01 17:30:00',
  '社区党群活动室',
  30,
  0,
  '3-12',
  1,
  0,
  0,
  NOW(),
  NOW(),
  0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM activity WHERE is_deleted = 0 AND title = '六一亲子手作派对'
);

INSERT INTO activity (
  title, description, type, image_url, start_time, end_time, location,
  max_participants, current_participants, age_limit, with_child_required, with_pet_required,
  status, create_time, update_time, is_deleted
)
SELECT
  '银龄茶话会与手机课堂',
  '面向长者居民开设智能手机常用功能讲解、社区服务小程序使用教学和防诈骗案例分享，安排志愿者一对一辅导。',
  '夕阳红',
  'https://images.unsplash.com/photo-1516307365426-bea591f05011?auto=format&fit=crop&w=1200&q=80',
  '2026-05-20 09:30:00',
  '2026-05-20 11:30:00',
  '长者服务中心一楼多功能厅',
  36,
  0,
  '>=55',
  0,
  0,
  0,
  NOW(),
  NOW(),
  0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM activity WHERE is_deleted = 0 AND title = '银龄茶话会与手机课堂'
);

INSERT INTO activity (
  title, description, type, image_url, start_time, end_time, location,
  max_participants, current_participants, age_limit, with_child_required, with_pet_required,
  status, create_time, update_time, is_deleted
)
SELECT
  '夜跑社群首次拉练',
  '社区夜跑小组首次集结，含热身指导、配速分组和跑后拉伸，适合日常想增强运动习惯的上班族。',
  '公益',
  'https://images.unsplash.com/photo-1486218119243-13883505764c?auto=format&fit=crop&w=1200&q=80',
  '2026-05-14 19:30:00',
  '2026-05-14 21:00:00',
  '环湖步道南门起点',
  20,
  0,
  '>=18',
  0,
  0,
  0,
  NOW(),
  NOW(),
  0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM activity WHERE is_deleted = 0 AND title = '夜跑社群首次拉练'
);

INSERT INTO activity (
  title, description, type, image_url, start_time, end_time, location,
  max_participants, current_participants, age_limit, with_child_required, with_pet_required,
  status, create_time, update_time, is_deleted
)
SELECT
  '春日邻里旧物交换市集',
  '已举办完成的旧物交换活动样例，现场设置闲置交换区、跳蚤摊位和志愿者服务台，营造真实运营痕迹。',
  '公益',
  'https://images.unsplash.com/photo-1520607162513-77705c0f0d4a?auto=format&fit=crop&w=1200&q=80',
  '2026-04-20 09:00:00',
  '2026-04-20 16:00:00',
  '一期架空层共享空间',
  40,
  0,
  '>=12',
  0,
  0,
  1,
  NOW(),
  NOW(),
  0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM activity WHERE is_deleted = 0 AND title = '春日邻里旧物交换市集'
);

INSERT INTO activity_registration (
  activity_id, user_id, nickname, phone, age, has_child, has_pet, remark, status, create_time, update_time, is_deleted
)
SELECT a.id, 1, '周宇轩', '13800000001', 34, 0, 0, '想顺便了解家里老人慢病用药。', 1, NOW(), NOW(), 0
FROM activity a
WHERE a.title = '周末便民义诊与血压筛查'
  AND NOT EXISTS (SELECT 1 FROM activity_registration r WHERE r.activity_id = a.id AND r.user_id = 1 AND r.is_deleted = 0);

INSERT INTO activity_registration (
  activity_id, user_id, nickname, phone, age, has_child, has_pet, remark, status, create_time, update_time, is_deleted
)
SELECT a.id, 2, '吕彬辰', '13800000002', 29, 0, 0, '想给父母预约血压检测。', 0, NOW(), NOW(), 0
FROM activity a
WHERE a.title = '周末便民义诊与血压筛查'
  AND NOT EXISTS (SELECT 1 FROM activity_registration r WHERE r.activity_id = a.id AND r.user_id = 2 AND r.is_deleted = 0);

INSERT INTO activity_registration (
  activity_id, user_id, nickname, phone, age, has_child, has_pet, remark, status, create_time, update_time, is_deleted
)
SELECT a.id, 32, '周嘉睿', '13000001184', 26, 0, 0, '第一次参加社区户外活动。', 1, NOW(), NOW(), 0
FROM activity a
WHERE a.title = '春末山野徒步打卡日'
  AND NOT EXISTS (SELECT 1 FROM activity_registration r WHERE r.activity_id = a.id AND r.user_id = 32 AND r.is_deleted = 0);

INSERT INTO activity_registration (
  activity_id, user_id, nickname, phone, age, has_child, has_pet, remark, status, create_time, update_time, is_deleted
)
SELECT a.id, 33, '吕可杰', '13000001221', 31, 0, 0, '希望安排中等配速组。', 0, NOW(), NOW(), 0
FROM activity a
WHERE a.title = '春末山野徒步打卡日'
  AND NOT EXISTS (SELECT 1 FROM activity_registration r WHERE r.activity_id = a.id AND r.user_id = 33 AND r.is_deleted = 0);

INSERT INTO activity_registration (
  activity_id, user_id, nickname, phone, age, has_child, has_pet, remark, status, create_time, update_time, is_deleted
)
SELECT a.id, 34, '王彬航', '13000001258', 23, 0, 0, '已经有登山装备。', 1, NOW(), NOW(), 0
FROM activity a
WHERE a.title = '春末山野徒步打卡日'
  AND NOT EXISTS (SELECT 1 FROM activity_registration r WHERE r.activity_id = a.id AND r.user_id = 34 AND r.is_deleted = 0);

INSERT INTO activity_registration (
  activity_id, user_id, nickname, phone, age, has_child, has_pet, remark, status, create_time, update_time, is_deleted
)
SELECT a.id, 39, '张林琳', '13000001443', 35, 1, 0, '会带 6 岁孩子参加，希望安排靠前草坪位。', 1, NOW(), NOW(), 0
FROM activity a
WHERE a.title = '亲子草坪露营体验营'
  AND NOT EXISTS (SELECT 1 FROM activity_registration r WHERE r.activity_id = a.id AND r.user_id = 39 AND r.is_deleted = 0);

INSERT INTO activity_registration (
  activity_id, user_id, nickname, phone, age, has_child, has_pet, remark, status, create_time, update_time, is_deleted
)
SELECT a.id, 40, '孙宇妍', '13000001480', 33, 1, 0, '孩子喜欢绘本活动。', 0, NOW(), NOW(), 0
FROM activity a
WHERE a.title = '亲子草坪露营体验营'
  AND NOT EXISTS (SELECT 1 FROM activity_registration r WHERE r.activity_id = a.id AND r.user_id = 40 AND r.is_deleted = 0);

INSERT INTO activity_registration (
  activity_id, user_id, nickname, phone, age, has_child, has_pet, remark, status, create_time, update_time, is_deleted
)
SELECT a.id, 41, '许明楠', '13000001517', 30, 1, 0, '可自带折叠椅。', 1, NOW(), NOW(), 0
FROM activity a
WHERE a.title = '亲子草坪露营体验营'
  AND NOT EXISTS (SELECT 1 FROM activity_registration r WHERE r.activity_id = a.id AND r.user_id = 41 AND r.is_deleted = 0);

INSERT INTO activity_registration (
  activity_id, user_id, nickname, phone, age, has_child, has_pet, remark, status, create_time, update_time, is_deleted
)
SELECT a.id, 42, '吴奕杰', '13000001554', 27, 0, 1, '会带一只已接种疫苗的柯基。', 1, NOW(), NOW(), 0
FROM activity a
WHERE a.title = '萌宠友好社交日'
  AND NOT EXISTS (SELECT 1 FROM activity_registration r WHERE r.activity_id = a.id AND r.user_id = 42 AND r.is_deleted = 0);

INSERT INTO activity_registration (
  activity_id, user_id, nickname, phone, age, has_child, has_pet, remark, status, create_time, update_time, is_deleted
)
SELECT a.id, 43, '冯若琪', '13000001591', 25, 0, 1, '想参加宠物义诊。', 0, NOW(), NOW(), 0
FROM activity a
WHERE a.title = '萌宠友好社交日'
  AND NOT EXISTS (SELECT 1 FROM activity_registration r WHERE r.activity_id = a.id AND r.user_id = 43 AND r.is_deleted = 0);

INSERT INTO activity_registration (
  activity_id, user_id, nickname, phone, age, has_child, has_pet, remark, status, create_time, update_time, is_deleted
)
SELECT a.id, 44, '钱安晨', '13000001628', 29, 0, 1, '希望认识更多附近养宠住户。', 1, NOW(), NOW(), 0
FROM activity a
WHERE a.title = '萌宠友好社交日'
  AND NOT EXISTS (SELECT 1 FROM activity_registration r WHERE r.activity_id = a.id AND r.user_id = 44 AND r.is_deleted = 0);

INSERT INTO activity_registration (
  activity_id, user_id, nickname, phone, age, has_child, has_pet, remark, status, create_time, update_time, is_deleted
)
SELECT a.id, 45, '周梓轩', '13000001665', 34, 1, 0, '一家三口参加。', 1, NOW(), NOW(), 0
FROM activity a
WHERE a.title = '六一亲子手作派对'
  AND NOT EXISTS (SELECT 1 FROM activity_registration r WHERE r.activity_id = a.id AND r.user_id = 45 AND r.is_deleted = 0);

INSERT INTO activity_registration (
  activity_id, user_id, nickname, phone, age, has_child, has_pet, remark, status, create_time, update_time, is_deleted
)
SELECT a.id, 46, '吕子辰', '13000001702', 32, 1, 0, '孩子 5 岁，喜欢手工。', 0, NOW(), NOW(), 0
FROM activity a
WHERE a.title = '六一亲子手作派对'
  AND NOT EXISTS (SELECT 1 FROM activity_registration r WHERE r.activity_id = a.id AND r.user_id = 46 AND r.is_deleted = 0);

INSERT INTO activity_registration (
  activity_id, user_id, nickname, phone, age, has_child, has_pet, remark, status, create_time, update_time, is_deleted
)
SELECT a.id, 47, '韩嘉萱', '13000001739', 31, 1, 0, '想报名亲子烘焙环节。', 1, NOW(), NOW(), 0
FROM activity a
WHERE a.title = '六一亲子手作派对'
  AND NOT EXISTS (SELECT 1 FROM activity_registration r WHERE r.activity_id = a.id AND r.user_id = 47 AND r.is_deleted = 0);

INSERT INTO activity_registration (
  activity_id, user_id, nickname, phone, age, has_child, has_pet, remark, status, create_time, update_time, is_deleted
)
SELECT a.id, 48, '褚雨妍', '13000001776', 58, 0, 0, '想学习手机挂号和线上缴费。', 1, NOW(), NOW(), 0
FROM activity a
WHERE a.title = '银龄茶话会与手机课堂'
  AND NOT EXISTS (SELECT 1 FROM activity_registration r WHERE r.activity_id = a.id AND r.user_id = 48 AND r.is_deleted = 0);

INSERT INTO activity_registration (
  activity_id, user_id, nickname, phone, age, has_child, has_pet, remark, status, create_time, update_time, is_deleted
)
SELECT a.id, 49, '唐星宇', '13000001813', 61, 0, 0, '需要志愿者协助设置微信支付。', 0, NOW(), NOW(), 0
FROM activity a
WHERE a.title = '银龄茶话会与手机课堂'
  AND NOT EXISTS (SELECT 1 FROM activity_registration r WHERE r.activity_id = a.id AND r.user_id = 49 AND r.is_deleted = 0);

INSERT INTO activity_registration (
  activity_id, user_id, nickname, phone, age, has_child, has_pet, remark, status, create_time, update_time, is_deleted
)
SELECT a.id, 50, '沈宇辰', '13000001850', 24, 0, 0, '准备和邻居一起参加夜跑。', 1, NOW(), NOW(), 0
FROM activity a
WHERE a.title = '夜跑社群首次拉练'
  AND NOT EXISTS (SELECT 1 FROM activity_registration r WHERE r.activity_id = a.id AND r.user_id = 50 AND r.is_deleted = 0);

INSERT INTO activity_registration (
  activity_id, user_id, nickname, phone, age, has_child, has_pet, remark, status, create_time, update_time, is_deleted
)
SELECT a.id, 51, '郑诗涵', '13000001887', 27, 0, 0, '平时 5 公里配速 6 分半。', 0, NOW(), NOW(), 0
FROM activity a
WHERE a.title = '夜跑社群首次拉练'
  AND NOT EXISTS (SELECT 1 FROM activity_registration r WHERE r.activity_id = a.id AND r.user_id = 51 AND r.is_deleted = 0);

INSERT INTO activity_registration (
  activity_id, user_id, nickname, phone, age, has_child, has_pet, remark, status, create_time, update_time, is_deleted
)
SELECT a.id, 52, '李昕月', '13000001924', 29, 0, 0, '旧物交换过一次体验很好。', 1, NOW(), NOW(), 0
FROM activity a
WHERE a.title = '春日邻里旧物交换市集'
  AND NOT EXISTS (SELECT 1 FROM activity_registration r WHERE r.activity_id = a.id AND r.user_id = 52 AND r.is_deleted = 0);

INSERT INTO activity_registration (
  activity_id, user_id, nickname, phone, age, has_child, has_pet, remark, status, create_time, update_time, is_deleted
)
SELECT a.id, 53, '何梓萌', '13000001961', 36, 0, 0, '摊位布置很方便，愿意下次继续参加。', 1, NOW(), NOW(), 0
FROM activity a
WHERE a.title = '春日邻里旧物交换市集'
  AND NOT EXISTS (SELECT 1 FROM activity_registration r WHERE r.activity_id = a.id AND r.user_id = 53 AND r.is_deleted = 0);

UPDATE activity a
JOIN (
  SELECT activity_id, COUNT(*) AS cnt
  FROM activity_registration
  WHERE is_deleted = 0 AND status IN (0, 1)
  GROUP BY activity_id
) r ON r.activity_id = a.id
SET a.current_participants = r.cnt,
    a.update_time = NOW()
WHERE a.is_deleted = 0;

UPDATE activity
SET current_participants = 0,
    update_time = NOW()
WHERE is_deleted = 0
  AND id NOT IN (
    SELECT DISTINCT activity_id
    FROM activity_registration
    WHERE is_deleted = 0 AND status IN (0, 1)
  );

UPDATE activity
SET status = 1,
    update_time = NOW()
WHERE is_deleted = 0
  AND end_time < NOW();

