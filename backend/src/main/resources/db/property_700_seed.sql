-- Generate 12 buildings * 2 units * 50 households = 1200 houses
-- Occupancy rate is random (~58%), around 700 owner records.
-- Execute on smart_community database.

USE smart_community;

DROP PROCEDURE IF EXISTS seed_property_households;
DELIMITER $$
CREATE PROCEDURE seed_property_households()
BEGIN
  DECLARE v_building INT DEFAULT 1;
  DECLARE v_unit INT DEFAULT 1;
  DECLARE v_room INT DEFAULT 1;
  DECLARE v_property_id BIGINT;
  DECLARE v_user_id BIGINT;
  DECLARE v_prop_offset BIGINT DEFAULT 0;
  DECLARE v_owner_offset BIGINT DEFAULT 0;
  DECLARE v_occupied INT DEFAULT 0;
  DECLARE v_area DECIMAL(10,2);
  DECLARE v_days INT;
  DECLARE v_reg DATETIME;
  DECLARE v_room_no VARCHAR(20);
  DECLARE v_owner_name VARCHAR(20);
  DECLARE v_property_code VARCHAR(32);
  DECLARE v_phone VARCHAR(32);

  SET @start_property_id := (SELECT IFNULL(MAX(id), 1000) + 1 FROM property);
  SET @start_user_id := (SELECT IFNULL(MAX(id), 10000) + 1 FROM `user`);
  SET v_days := DATEDIFF('2026-12-31', '2009-01-01');

  WHILE v_building <= 12 DO
    SET v_unit = 1;
    WHILE v_unit <= 2 DO
      SET v_room = 1;
      WHILE v_room <= 50 DO
        SET v_property_id = @start_property_id + v_prop_offset;
        SET v_prop_offset = v_prop_offset + 1;

        SET v_room_no = LPAD(100 + v_room, 3, '0');
        SET v_area = ROUND(49 + RAND() * (135 - 49), 2);
        SET v_reg = DATE_ADD('2009-01-01', INTERVAL FLOOR(RAND() * (v_days + 1)) DAY);
        SET v_occupied = IF(RAND() < 0.58, 1, 0);
        SET v_owner_name = IF(v_occupied = 1, CONCAT('业主', LPAD(v_property_id, 4, '0')), '');

        SET v_property_code = CONCAT(
          'YZ',
          LPAD(v_building, 2, '0'),
          LPAD(v_unit, 2, '0'),
          LPAD(CAST(v_room_no AS UNSIGNED), 3, '0'),
          DATE_FORMAT(v_reg, '%y')
        );

        INSERT INTO property (
          id, community, building, unit, room, property_code, owner_name, area, status, create_time, update_time, is_deleted
        ) VALUES (
          v_property_id, 'Smart Garden', CAST(v_building AS CHAR), CAST(v_unit AS CHAR), v_room_no, v_property_code,
          v_owner_name, v_area, IF(v_occupied = 1, 4, 3), v_reg, v_reg, 0
        );

        IF v_occupied = 1 THEN
          SET v_user_id = @start_user_id + v_owner_offset;
          SET v_owner_offset = v_owner_offset + 1;

          SET v_phone = CONCAT('13', LPAD(MOD(v_user_id * 37, 1000000000), 9, '0'));

          INSERT INTO `user` (
            id, openid, unionid, account, password, must_change_password, role, nickname, avatar_url, phone, points, status, create_time, update_time, is_deleted
          ) VALUES (
            v_user_id,
            CONCAT('seed-owner-', v_user_id),
            CONCAT('seed-union-', v_user_id),
            v_property_code,
            REVERSE(RIGHT(CONCAT('000000', REGEXP_REPLACE(v_property_code, '[^0-9]', '')), 6)),
            1,
            1,
            v_owner_name,
            NULL,
            v_phone,
            0,
            1,
            v_reg,
            v_reg,
            0
          );

          INSERT INTO user_property (
            user_id, property_id, relation, is_primary, create_time, update_time, is_deleted
          ) VALUES (
            v_user_id, v_property_id, 'self', 1, v_reg, v_reg, 0
          );
        END IF;

        SET v_room = v_room + 1;
      END WHILE;
      SET v_unit = v_unit + 1;
    END WHILE;
    SET v_building = v_building + 1;
  END WHILE;
END$$
DELIMITER ;

CALL seed_property_households();
DROP PROCEDURE IF EXISTS seed_property_households;

SELECT
  COUNT(*) AS property_total,
  SUM(CASE WHEN status = 4 THEN 1 ELSE 0 END) AS occupied_count,
  ROUND(SUM(CASE WHEN status = 4 THEN 1 ELSE 0 END) / COUNT(*) * 100, 2) AS occupancy_rate_pct
FROM property;
