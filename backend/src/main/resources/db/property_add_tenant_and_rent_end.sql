SET NAMES utf8mb4;
USE smart_community;

SET @col_tenant_exists := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'property'
    AND column_name = 'tenant_name'
);
SET @sql_add_tenant := IF(
  @col_tenant_exists = 0,
  'ALTER TABLE property ADD COLUMN tenant_name VARCHAR(64) DEFAULT NULL AFTER owner_name',
  'SELECT 1'
);
PREPARE stmt_add_tenant FROM @sql_add_tenant;
EXECUTE stmt_add_tenant;
DEALLOCATE PREPARE stmt_add_tenant;

SET @col_rent_end_exists := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'property'
    AND column_name = 'rent_end_time'
);
SET @sql_add_rent_end := IF(
  @col_rent_end_exists = 0,
  'ALTER TABLE property ADD COLUMN rent_end_time DATETIME DEFAULT NULL AFTER tenant_name',
  'SELECT 1'
);
PREPARE stmt_add_rent_end FROM @sql_add_rent_end;
EXECUTE stmt_add_rent_end;
DEALLOCATE PREPARE stmt_add_rent_end;

-- 非已出租状态不保留租户信息（兜底清洗）
UPDATE property
SET tenant_name = NULL,
    rent_end_time = NULL,
    update_time = NOW()
WHERE is_deleted = 0
  AND status <> 5
  AND (tenant_name IS NOT NULL OR rent_end_time IS NOT NULL);

-- 已出租但缺租户信息的，补齐租户与截止日期（模拟真实数据）
UPDATE property p
SET p.tenant_name = CONCAT(
      ELT(MOD(p.id * 19 + 5, 18) + 1, '刘','黄','谢','邓','宋','唐','韩','曹','许','程','袁','彭','叶','阎','苏','傅','姜','潘'),
      ELT(MOD(p.id * 23 + 9, 14) + 1, '子','雨','欣','浩','晨','嘉','依','博','若','俊','可','奕','思','文'),
      ELT(MOD(p.id * 7 + 3, 12) + 1, '涵','宁','轩','然','琳','婷','峰','杰','妍','航','宇','彤')
    ),
    p.rent_end_time = COALESCE(p.rent_end_time, DATE_ADD(CURDATE(), INTERVAL (MOD(p.id, 360) + 30) DAY)),
    p.update_time = NOW()
WHERE p.is_deleted = 0
  AND p.status = 5
  AND (p.tenant_name IS NULL OR p.tenant_name = '' OR p.rent_end_time IS NULL);

SELECT
  status,
  COUNT(*) AS total,
  SUM(CASE WHEN tenant_name IS NOT NULL AND tenant_name <> '' THEN 1 ELSE 0 END) AS with_tenant
FROM property
WHERE is_deleted = 0
GROUP BY status
ORDER BY status;
