-- docker exec -i drop-commerce-mariadb-1 sh -c 'mariadb -uroot -p"$MARIADB_ROOT_PASSWORD" drop_commerce -t' < dummy/drop-dummy-check.sql (Git Bash)

SET NAMES utf8mb4;

SELECT (SELECT COUNT(*) FROM app_user)     AS app_user,
       (SELECT COUNT(*) FROM store)        AS store,
       (SELECT COUNT(*) FROM product)      AS product,
       (SELECT COUNT(*) FROM product_drop) AS product_drop;

SELECT CASE
           WHEN start_at > NOW() THEN 'UPCOMING'
           WHEN end_at > NOW()   THEN 'ONGOING'
           ELSE 'ENDED'
       END AS phase,
       status,
       COUNT(*) AS cnt
FROM product_drop
GROUP BY phase, status
ORDER BY phase, status;

-- 넣은 날과 측정한 날 같아야 함
SELECT COUNT(*) AS default_list_rows
FROM product_drop
WHERE approved_at IS NOT NULL AND end_at > NOW();

SELECT AVG(limited_quantity)    AS avg_limited,
       SUM(limited_quantity)    AS sum_limited,
       COUNT(DISTINCT start_at) AS distinct_start_at
FROM product_drop;

-- 전부 0
SELECT SUM(remaining_quantity > limited_quantity)                              AS remaining_over_limited,
       SUM(max_quantity_per_user > limited_quantity)                           AS per_user_over_limited,
       SUM(status = 'APPROVED' AND end_at <= NOW() AND remaining_quantity > 0) AS ended_not_sold_out,
       SUM(status IN ('PENDING', 'REJECTED') AND approved_at IS NOT NULL)      AS unapproved_with_approved_at,
       SUM(approved_at > NOW())                                                AS approved_in_future
FROM product_drop;

-- 기본 목록 첫 페이지
SELECT COUNT(DISTINCT p.store_id) AS stores_on_first_page,
       COUNT(DISTINCT p.id)       AS products_on_first_page
FROM (SELECT product_id
      FROM product_drop
      WHERE approved_at IS NOT NULL AND end_at > NOW()
      ORDER BY start_at, id
      LIMIT 20) first_page
JOIN product p ON p.id = first_page.product_id;
