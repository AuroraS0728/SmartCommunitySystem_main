SET NAMES utf8mb4;
USE smart_community;

-- 目标：
-- 1) 未售(status=1) 保持无业主
-- 2) 其余状态(status=2/3/4/5) 全部补齐业主名
-- 3) 业主名按稳定伪随机规则生成，避免按ID查看时出现连续编号感

START TRANSACTION;

UPDATE property p
SET p.owner_name = CONCAT(
    ELT(MOD(p.id * 17 + 3, 24) + 1,
      '赵','钱','孙','李','周','吴','郑','王','冯','陈','褚','卫',
      '蒋','沈','韩','杨','朱','秦','尤','许','何','吕','施','张'
    ),
    ELT(MOD(p.id * 31 + 7, 26) + 1,
      '梓','浩','宇','欣','雨','子','若','明','文','思','嘉','安','晨',
      '林','依','可','博','奕','诗','彦','辰','宁','悦','诚','宸','彬'
    ),
    ELT(MOD(p.id * 13 + 11, 16) + 1,
      '轩','然','涵','宁','婷','杰','峰','琳','睿','晨','萱','楠','琪','辰','妍','航'
    )
)
WHERE p.is_deleted = 0
  AND p.status IN (2, 3, 4, 5);

-- 未售清空业主（再次兜底）
UPDATE property
SET owner_name = '',
    update_time = NOW()
WHERE is_deleted = 0
  AND status = 1
  AND owner_name IS NOT NULL
  AND owner_name <> '';

COMMIT;

-- 验收1：各状态是否有业主
SELECT
  status,
  COUNT(*) AS total,
  SUM(CASE WHEN owner_name IS NOT NULL AND owner_name <> '' THEN 1 ELSE 0 END) AS has_owner
FROM property
WHERE is_deleted = 0
GROUP BY status
ORDER BY status;

-- 验收2：抽样查看“已入住”按ID排序后的业主名，确保不连贯
SELECT id, owner_name
FROM property
WHERE is_deleted = 0
  AND status = 4
ORDER BY id
LIMIT 30;
