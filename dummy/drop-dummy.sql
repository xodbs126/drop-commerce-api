-- F-35 측정용 더미 데이터: 판매점 500, 상품 5,000, 드롭 60,000
-- docker exec -i drop-commerce-mariadb-1 sh -c 'mariadb -uroot -p"$MARIADB_ROOT_PASSWORD" drop_commerce' < dummy/drop-dummy.sql (Git Bash)
-- 빈 테이블에서 실행할 것 (id를 직접 넣음)

SET NAMES utf8mb4;

-- 판매점 수 최대 500. 소량 확인은 5
SET @store_count = 500;
SET @products_per_store = 10;
SET @drops_per_store = 120;

-- 6만 개 기준 진행 중·예정 드롭 수
SET @ongoing_per_60000 = 170;
SET @upcoming_per_60000 = 1670;

SET @ended_from    = TIMESTAMP '2023-11-01 00:00:00';
SET @ended_to      = TIMESTAMP '2026-10-25 00:00:00';
SET @ongoing_from  = TIMESTAMP '2026-06-01 00:00:00';
SET @ongoing_to    = TIMESTAMP '2026-10-31 00:00:00';
SET @ongoing_end   = TIMESTAMP '2030-01-01 00:00:00';
SET @upcoming_from = TIMESTAMP '2031-01-01 00:00:00';
SET @upcoming_to   = TIMESTAMP '2031-12-31 00:00:00';
SET @approved_cap  = TIMESTAMP '2026-11-01 00:00:00';

SET @dummy_password = 'dummy-no-login';

SET @product_count  = @store_count * @products_per_store;
SET @drop_count     = @store_count * @drops_per_store;
SET @ongoing_count  = ROUND(@drop_count * @ongoing_per_60000 / 60000);
SET @upcoming_count = ROUND(@drop_count * @upcoming_per_60000 / 60000);
SET @ended_count    = @drop_count - @ongoing_count - @upcoming_count;

SET @ended_step    = TIMESTAMPDIFF(MINUTE, @ended_from, @ended_to) DIV GREATEST(@ended_count, 1);
SET @ongoing_step  = TIMESTAMPDIFF(MINUTE, @ongoing_from, @ongoing_to) DIV GREATEST(@ongoing_count, 1);
SET @upcoming_step = TIMESTAMPDIFF(MINUTE, @upcoming_from, @upcoming_to) DIV GREATEST(@upcoming_count, 1);

START TRANSACTION;

INSERT INTO app_user (id, username, password)
SELECT seq,
       CONCAT('dummy_owner_', LPAD(seq, 3, '0')),
       @dummy_password
FROM seq_1_to_500
WHERE seq <= @store_count;

INSERT INTO store (id, owner_app_user_id, name)
SELECT seq,
       seq,
       CONCAT('더미 판매점 ', LPAD(seq, 3, '0'))
FROM seq_1_to_500
WHERE seq <= @store_count;

INSERT INTO product (id, store_id, name, description, category, price, stock)
SELECT seq,
       (seq - 1) DIV @products_per_store + 1,
       CONCAT(ELT(1 + MOD(seq, 4), '식품', '도서', '티켓', '상품권'), ' 상품 ', LPAD(seq, 4, '0')),
       CONCAT('더미 설명 ',
              ELT(1 + MOD(seq DIV 4, 10), '방수', '한정', '친환경', '수제', '선착순',
                                          '초판', '굿즈', '사인', '리미티드', '신상')),
       ELT(1 + MOD(seq, 4), 'FOOD', 'BOOK', 'TICKET', 'VOUCHER'),
       1000 * (1 + MOD(seq * 13, 100)),
       500
FROM seq_1_to_5000
WHERE seq <= @product_count;

-- d가 커질수록 시작 시각이 늦음 (종료 → 진행 중 → 예정)
-- 이웃한 d는 다른 판매점, 상품 하나당 드롭 12개
INSERT INTO product_drop (id, product_id, limited_quantity, remaining_quantity, max_quantity_per_user,
                          start_at, end_at, approved_at, status, is_returned)
SELECT d,
       product_id,
       limited,
       CASE
           WHEN kind = 'APPROVED' AND phase = 'ENDED'   THEN 0
           WHEN kind = 'APPROVED' AND phase = 'ONGOING' THEN MOD(d * 7, limited + 1)
           ELSE limited
       END,
       1 + MOD(d, 5),
       start_at,
       CASE phase
           WHEN 'ONGOING' THEN @ongoing_end + INTERVAL MOD(d, 365) DAY
           ELSE start_at + INTERVAL (1 + MOD(d, 5)) DAY
       END,
       CASE
           WHEN kind IN ('APPROVED', 'CANCELLED_APPROVED')
               THEN LEAST(start_at - INTERVAL (7 + MOD(d, 21)) DAY, @approved_cap)
       END,
       CASE kind
           WHEN 'CANCELLED_APPROVED' THEN 'CANCELLED'
           WHEN 'CANCELLED_PENDING'  THEN 'CANCELLED'
           ELSE kind
       END,
       (kind = 'APPROVED' AND phase = 'ENDED') OR kind = 'CANCELLED_APPROVED'
FROM (
    SELECT d, product_id, limited, phase, kind,
           CASE phase
               WHEN 'ENDED'   THEN @ended_from    + INTERVAL ((d - 1) * @ended_step) MINUTE
               WHEN 'ONGOING' THEN @ongoing_from  + INTERVAL ((d - @ended_count - 1) * @ongoing_step) MINUTE
               ELSE                @upcoming_from + INTERVAL ((d - @ended_count - @ongoing_count - 1) * @upcoming_step) MINUTE
           END AS start_at
    FROM (
        SELECT d, product_id, limited, phase,
               CASE
                   WHEN MOD(d, 100) IN (0, 1)                 THEN 'REJECTED'
                   WHEN MOD(d, 100) = 2                       THEN 'CANCELLED_APPROVED'
                   WHEN MOD(d, 100) = 3                       THEN 'CANCELLED_PENDING'
                   WHEN phase = 'UPCOMING' AND MOD(d, 5) = 0  THEN 'PENDING'
                   ELSE 'APPROVED'
               END AS kind
        FROM (
            SELECT seq AS d,
                   MOD(seq - 1, @store_count) * @products_per_store
                       + MOD((seq - 1) DIV @store_count, @products_per_store) + 1 AS product_id,
                   10 + MOD(seq * 37, 181) AS limited,
                   CASE
                       WHEN seq <= @ended_count                  THEN 'ENDED'
                       WHEN seq <= @ended_count + @ongoing_count THEN 'ONGOING'
                       ELSE 'UPCOMING'
                   END AS phase
            FROM seq_1_to_60000
            WHERE seq <= @drop_count
        ) numbered
    ) with_kind
) with_start;

COMMIT;
