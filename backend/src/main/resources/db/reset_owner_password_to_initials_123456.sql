SET NAMES utf8mb4;
USE smart_community;

DROP FUNCTION IF EXISTS fn_initial_char;
DELIMITER $$
CREATE FUNCTION fn_initial_char(ch VARCHAR(8))
RETURNS VARCHAR(1)
DETERMINISTIC
BEGIN
  DECLARE gbk_bytes VARBINARY(8);
  DECLARE byte1 INT;
  DECLARE byte2 INT;
  DECLARE code_no INT;

  IF ch IS NULL OR ch = '' THEN
    RETURN '';
  END IF;

  IF ch REGEXP '^[A-Za-z]$' THEN
    RETURN UPPER(ch);
  END IF;

  SET gbk_bytes = CONVERT(ch USING gbk);
  IF LENGTH(gbk_bytes) < 2 THEN
    RETURN '';
  END IF;

  SET byte1 = CONV(HEX(SUBSTRING(gbk_bytes, 1, 1)), 16, 10);
  SET byte2 = CONV(HEX(SUBSTRING(gbk_bytes, 2, 1)), 16, 10);
  SET code_no = (byte1 - 160) * 100 + (byte2 - 160);

  IF code_no >= 1601 AND code_no < 1637 THEN RETURN 'A'; END IF;
  IF code_no >= 1637 AND code_no < 1833 THEN RETURN 'B'; END IF;
  IF code_no >= 1833 AND code_no < 2078 THEN RETURN 'C'; END IF;
  IF code_no >= 2078 AND code_no < 2274 THEN RETURN 'D'; END IF;
  IF code_no >= 2274 AND code_no < 2302 THEN RETURN 'E'; END IF;
  IF code_no >= 2302 AND code_no < 2433 THEN RETURN 'F'; END IF;
  IF code_no >= 2433 AND code_no < 2594 THEN RETURN 'G'; END IF;
  IF code_no >= 2594 AND code_no < 2787 THEN RETURN 'H'; END IF;
  IF code_no >= 2787 AND code_no < 3106 THEN RETURN 'J'; END IF;
  IF code_no >= 3106 AND code_no < 3212 THEN RETURN 'K'; END IF;
  IF code_no >= 3212 AND code_no < 3472 THEN RETURN 'L'; END IF;
  IF code_no >= 3472 AND code_no < 3635 THEN RETURN 'M'; END IF;
  IF code_no >= 3635 AND code_no < 3722 THEN RETURN 'N'; END IF;
  IF code_no >= 3722 AND code_no < 3730 THEN RETURN 'O'; END IF;
  IF code_no >= 3730 AND code_no < 3858 THEN RETURN 'P'; END IF;
  IF code_no >= 3858 AND code_no < 4027 THEN RETURN 'Q'; END IF;
  IF code_no >= 4027 AND code_no < 4086 THEN RETURN 'R'; END IF;
  IF code_no >= 4086 AND code_no < 4390 THEN RETURN 'S'; END IF;
  IF code_no >= 4390 AND code_no < 4558 THEN RETURN 'T'; END IF;
  IF code_no >= 4558 AND code_no < 4684 THEN RETURN 'W'; END IF;
  IF code_no >= 4684 AND code_no < 4925 THEN RETURN 'X'; END IF;
  IF code_no >= 4925 AND code_no < 5249 THEN RETURN 'Y'; END IF;
  IF code_no >= 5249 AND code_no < 5600 THEN RETURN 'Z'; END IF;
  RETURN 'X';
END$$
DELIMITER ;

DROP FUNCTION IF EXISTS fn_name_initials;
DELIMITER $$
CREATE FUNCTION fn_name_initials(name_text VARCHAR(128))
RETURNS VARCHAR(64)
DETERMINISTIC
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE n INT DEFAULT 0;
  DECLARE ch VARCHAR(8);
  DECLARE out_text VARCHAR(64) DEFAULT '';
  DECLARE latin_open TINYINT DEFAULT 0;
  DECLARE char_init VARCHAR(1);

  SET name_text = TRIM(COALESCE(name_text, ''));
  SET n = CHAR_LENGTH(name_text);
  WHILE i <= n DO
    SET ch = SUBSTRING(name_text, i, 1);
    IF ch REGEXP '^[A-Za-z]$' THEN
      IF latin_open = 0 THEN
        SET out_text = CONCAT(out_text, UPPER(ch));
        SET latin_open = 1;
      END IF;
    ELSE
      SET latin_open = 0;
      SET char_init = fn_initial_char(ch);
      IF char_init <> '' THEN
        SET out_text = CONCAT(out_text, char_init);
      END IF;
    END IF;
    SET i = i + 1;
  END WHILE;

  IF out_text = '' THEN
    SET out_text = 'YZ';
  END IF;
  RETURN out_text;
END$$
DELIMITER ;

UPDATE `user` u
LEFT JOIN `user_property` up ON up.`user_id` = u.`id`
  AND up.`is_deleted` = 0
  AND up.`is_primary` = 1
LEFT JOIN `property` p ON p.`id` = up.`property_id`
  AND p.`is_deleted` = 0
SET u.`password` = CONCAT(
      fn_name_initials(COALESCE(NULLIF(TRIM(p.`owner_name`), ''), NULLIF(TRIM(u.`nickname`), ''))),
      '123456'
    ),
    u.`must_change_password` = 1,
    u.`update_time` = NOW()
WHERE u.`role` = 1
  AND u.`is_deleted` = 0;

SELECT ROW_COUNT() AS updated_owner_rows;
