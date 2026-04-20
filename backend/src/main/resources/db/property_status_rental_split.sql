SET NAMES utf8mb4;
USE smart_community;

-- 房屋状态定义：
-- 1=未售 2=已售 3=空置 4=已入住 5=已出租
--
-- 目标（按有效房屋 is_deleted=0，按 id 升序稳定分配）：
-- 5 已出租：200 套
-- 4 已入住：70 套
-- 2 已售：100 套
-- 1 未售：130 套
-- 其余：3 空置

START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS tmp_property_rank;
CREATE TEMPORARY TABLE tmp_property_rank AS
SELECT
  id,
  ROW_NUMBER() OVER (ORDER BY id) AS rn
FROM property
WHERE is_deleted = 0;

UPDATE property p
JOIN tmp_property_rank t ON t.id = p.id
SET
  p.status = CASE
    WHEN t.rn <= 200 THEN 5
    WHEN t.rn <= 270 THEN 4
    WHEN t.rn <= 370 THEN 2
    WHEN t.rn <= 500 THEN 1
    ELSE 3
  END,
  p.update_time = NOW();

DROP TEMPORARY TABLE IF EXISTS tmp_property_rank;

COMMIT;

-- 验收统计
SELECT status, COUNT(*) AS cnt
FROM property
WHERE is_deleted = 0
GROUP BY status
ORDER BY status;
