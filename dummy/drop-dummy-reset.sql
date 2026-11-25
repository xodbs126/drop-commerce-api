-- dummy_owner_ 판매점 아래 데이터만 지움. 주문이 있으면 FK 때문에 실패함
-- docker exec -i drop-commerce-mariadb-1 sh -c 'mariadb -uroot -p"$MARIADB_ROOT_PASSWORD" drop_commerce' < dummy/drop-dummy-reset.sql (Git Bash)

START TRANSACTION;

DELETE pd
FROM product_drop pd
JOIN product p  ON p.id = pd.product_id
JOIN store s    ON s.id = p.store_id
JOIN app_user u ON u.id = s.owner_app_user_id
WHERE u.username LIKE 'dummy\_owner\_%';

DELETE p
FROM product p
JOIN store s    ON s.id = p.store_id
JOIN app_user u ON u.id = s.owner_app_user_id
WHERE u.username LIKE 'dummy\_owner\_%';

DELETE s
FROM store s
JOIN app_user u ON u.id = s.owner_app_user_id
WHERE u.username LIKE 'dummy\_owner\_%';

DELETE FROM app_user
WHERE username LIKE 'dummy\_owner\_%';

COMMIT;
