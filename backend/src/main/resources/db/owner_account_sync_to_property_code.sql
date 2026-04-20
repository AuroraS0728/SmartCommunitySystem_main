SET NAMES utf8mb4;
USE smart_community;

ALTER TABLE `user`
  ADD COLUMN IF NOT EXISTS `must_change_password` TINYINT(1) NOT NULL DEFAULT 0;

-- Ensure property_code follows: YZ + building(2) + unit(2) + room(3) + year(2)
UPDATE `property`
SET `property_code` = CONCAT(
    'YZ',
    LPAD(CAST(COALESCE(NULLIF(REGEXP_SUBSTR(`building`, '[0-9]+'), ''), '0') AS UNSIGNED) % 100, 2, '0'),
    LPAD(CAST(COALESCE(NULLIF(REGEXP_SUBSTR(`unit`, '[0-9]+'), ''), '0') AS UNSIGNED) % 100, 2, '0'),
    LPAD(CAST(COALESCE(NULLIF(REGEXP_SUBSTR(`room`, '[0-9]+'), ''), '0') AS UNSIGNED) % 1000, 3, '0'),
    DATE_FORMAT(COALESCE(`create_time`, NOW()), '%y')
)
WHERE `is_deleted` = 0
  AND (`property_code` IS NULL OR `property_code` = '' OR `property_code` NOT REGEXP '^YZ[0-9]{9}$');

-- Align owner account with property_code.
UPDATE `user` u
JOIN `user_property` up ON up.`user_id` = u.`id`
  AND up.`is_deleted` = 0
  AND up.`is_primary` = 1
JOIN `property` p ON p.`id` = up.`property_id`
  AND p.`is_deleted` = 0
SET u.`account` = p.`property_code`,
    u.`update_time` = NOW()
WHERE u.`role` = 1
  AND u.`is_deleted` = 0
  AND p.`property_code` IS NOT NULL
  AND p.`property_code` <> '';

-- Initialize password by current account last 6 digits reversed (only when password is empty).
UPDATE `user` u
JOIN `user_property` up ON up.`user_id` = u.`id`
  AND up.`is_deleted` = 0
  AND up.`is_primary` = 1
JOIN `property` p ON p.`id` = up.`property_id`
  AND p.`is_deleted` = 0
SET u.`password` = REVERSE(RIGHT(CONCAT(REGEXP_REPLACE(COALESCE(u.`account`, ''), '[^0-9]', ''), '000000'), 6)),
    u.`must_change_password` = 1,
    u.`update_time` = NOW()
WHERE u.`role` = 1
  AND u.`is_deleted` = 0
  AND p.`property_code` IS NOT NULL
  AND p.`property_code` <> ''
  AND u.`account` IS NOT NULL
  AND u.`account` <> ''
  AND (u.`password` IS NULL OR u.`password` = '');
