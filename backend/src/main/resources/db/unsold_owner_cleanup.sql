SET NAMES utf8mb4;
USE smart_community;

-- 目标：
-- status=1(未售) 的房屋不应有业主信息和业主-房屋绑定关系

START TRANSACTION;

-- 1) 清空未售房屋业主姓名
UPDATE property
SET owner_name = '',
    update_time = NOW()
WHERE is_deleted = 0
  AND status = 1
  AND owner_name IS NOT NULL
  AND owner_name <> '';

-- 2) 清理未售房屋的有效绑定关系（逻辑删除）
UPDATE user_property up
JOIN property p ON p.id = up.property_id
SET up.is_deleted = 1,
    up.update_time = NOW()
WHERE up.is_deleted = 0
  AND p.is_deleted = 0
  AND p.status = 1;

COMMIT;

-- 验收
SELECT
  COUNT(*) AS total_unsold,
  SUM(CASE WHEN owner_name IS NOT NULL AND owner_name <> '' THEN 1 ELSE 0 END) AS unsold_with_owner
FROM property
WHERE is_deleted = 0
  AND status = 1;

SELECT COUNT(*) AS unsold_active_bind
FROM user_property up
JOIN property p ON p.id = up.property_id
WHERE up.is_deleted = 0
  AND p.is_deleted = 0
  AND p.status = 1;
