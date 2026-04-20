SET NAMES utf8mb4;
USE smart_community;

-- 历史数据回填：
-- 把房屋 owner_name 同步到主绑定业主(user.role=1, user_property.is_primary=1)的 user.nickname
-- 未售(status=1)不参与同步

START TRANSACTION;

UPDATE `user` u
JOIN user_property up ON up.user_id = u.id
  AND up.is_deleted = 0
  AND up.is_primary = 1
JOIN property p ON p.id = up.property_id
  AND p.is_deleted = 0
  AND p.status <> 1
SET u.nickname = p.owner_name,
    u.update_time = NOW()
WHERE u.is_deleted = 0
  AND u.role = 1
  AND p.owner_name IS NOT NULL
  AND p.owner_name <> ''
  AND IFNULL(u.nickname, '') <> p.owner_name;

COMMIT;

-- 验收：剩余不一致条数
SELECT COUNT(*) AS mismatch_cnt
FROM user_property up
JOIN property p ON p.id = up.property_id AND p.is_deleted = 0
JOIN `user` u ON u.id = up.user_id AND u.is_deleted = 0
WHERE up.is_deleted = 0
  AND up.is_primary = 1
  AND u.role = 1
  AND p.status <> 1
  AND p.owner_name IS NOT NULL
  AND p.owner_name <> ''
  AND IFNULL(u.nickname, '') <> p.owner_name;
