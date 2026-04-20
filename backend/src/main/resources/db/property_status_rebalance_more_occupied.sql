SET NAMES utf8mb4;
USE smart_community;

-- 状态定义：
-- 1=未售 2=已售 3=空置 4=已入住 5=已出租
--
-- 本次目标：
-- 5 已出租：200
-- 4 已入住：643（提高）
-- 2 已售：100
-- 1 未售：130
-- 3 空置：130（100多套）

START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS tmp_property_rank;
CREATE TEMPORARY TABLE tmp_property_rank AS
SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn
FROM property
WHERE is_deleted = 0;

UPDATE property p
JOIN tmp_property_rank t ON t.id = p.id
SET p.status = CASE
  WHEN t.rn <= 200 THEN 5
  WHEN t.rn <= 843 THEN 4
  WHEN t.rn <= 943 THEN 2
  WHEN t.rn <= 1073 THEN 1
  ELSE 3
END,
p.update_time = NOW();

DROP TEMPORARY TABLE IF EXISTS tmp_property_rank;

COMMIT;

SELECT status, COUNT(*) AS cnt
FROM property
WHERE is_deleted = 0
GROUP BY status
ORDER BY status;
