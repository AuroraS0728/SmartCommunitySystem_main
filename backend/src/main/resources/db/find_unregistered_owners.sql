-- Find owners who do not have any active resident account binding.
-- Rule: property exists, but there is no valid role=1 user bound to it.

SELECT
    p.id AS property_id,
    p.community,
    p.building,
    p.unit,
    p.room,
    p.property_code,
    p.owner_name,
    p.area,
    p.status AS property_status,
    p.create_time
FROM property p
LEFT JOIN user_property up
    ON up.property_id = p.id
    AND up.is_deleted = 0
LEFT JOIN `user` u
    ON u.id = up.user_id
    AND u.is_deleted = 0
    AND u.role = 1
WHERE p.is_deleted = 0
GROUP BY
    p.id,
    p.community,
    p.building,
    p.unit,
    p.room,
    p.property_code,
    p.owner_name,
    p.area,
    p.status,
    p.create_time
HAVING COUNT(u.id) = 0
ORDER BY p.community, p.building, p.unit, p.room;

-- Count only.
SELECT COUNT(*) AS unregistered_owner_count
FROM (
    SELECT p.id
    FROM property p
    LEFT JOIN user_property up
        ON up.property_id = p.id
        AND up.is_deleted = 0
    LEFT JOIN `user` u
        ON u.id = up.user_id
        AND u.is_deleted = 0
        AND u.role = 1
    WHERE p.is_deleted = 0
    GROUP BY p.id
    HAVING COUNT(u.id) = 0
) t;
