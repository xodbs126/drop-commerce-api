-- Flyway V1: Initial schema for drop commerce
-- Source: Domain model / ERD design, confirmed 2026-10-18 (6 tables)
-- Target: MariaDB / InnoDB
--
-- Design notes:
-- 1) app_user includes id only; authentication columns will be decided in F-11/F-12.
-- 2) created_at is supplied by the application Clock (no DB NOW() default).
-- 3) No product inventory table, order-item table, or separate drop-application table.
-- 4) ON DELETE RESTRICT is intentional: cancellations are status updates, not deletes.
-- 5) Domain state names are constrained to those specified in the ERD.
-- 6) VARCHAR lengths, NOT NULL and default choices are initial implementation assumptions.
--
-- Flyway creates/uses the configured database; this file does not CREATE DATABASE.

CREATE TABLE app_user (
                          id BIGINT NOT NULL AUTO_INCREMENT,

                          CONSTRAINT pk_app_user PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE store_application (
                                   id BIGINT NOT NULL AUTO_INCREMENT,
                                   app_user_id BIGINT NOT NULL,
                                   status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                   store_name VARCHAR(100) NOT NULL,
                                   store_description TEXT NOT NULL,
                                   created_at DATETIME NOT NULL,

    -- Only PENDING / APPROVED requests take part in the unique index.
    -- REJECTED rows evaluate to NULL, which MariaDB UNIQUE allows repeatedly.
                                   pending_or_approved_user_id BIGINT
                                       GENERATED ALWAYS AS (
                                           CASE
                                               WHEN status IN ('PENDING', 'APPROVED') THEN app_user_id
                                               ELSE NULL
                                               END
                                           ) VIRTUAL,

                                   CONSTRAINT pk_store_application PRIMARY KEY (id),
                                   CONSTRAINT uk_store_application_pending_or_approved_user
                                       UNIQUE (pending_or_approved_user_id),
                                   CONSTRAINT fk_store_application_app_user
                                       FOREIGN KEY (app_user_id) REFERENCES app_user (id)
                                           ON DELETE RESTRICT ON UPDATE RESTRICT,
                                   CONSTRAINT ck_store_application_status
                                       CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
                                   INDEX idx_store_application_app_user_id (app_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE store (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       owner_app_user_id BIGINT NOT NULL,
                       name VARCHAR(100) NOT NULL,

                       CONSTRAINT pk_store PRIMARY KEY (id),
                       CONSTRAINT uk_store_owner_app_user UNIQUE (owner_app_user_id),
                       CONSTRAINT fk_store_owner_app_user
                           FOREIGN KEY (owner_app_user_id) REFERENCES app_user (id)
                               ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE product (
                         id BIGINT NOT NULL AUTO_INCREMENT,
                         store_id BIGINT NOT NULL,
                         name VARCHAR(150) NOT NULL,
                         description TEXT NOT NULL,
                         category VARCHAR(50) NOT NULL,
                         price INT NOT NULL,
                         stock INT NOT NULL DEFAULT 0,
                         deleted_at DATETIME NULL,

                         CONSTRAINT pk_product PRIMARY KEY (id),
                         CONSTRAINT ck_product_stock_nonnegative CHECK (stock >= 0),
                         CONSTRAINT fk_product_store
                             FOREIGN KEY (store_id) REFERENCES store (id)
                                 ON DELETE RESTRICT ON UPDATE RESTRICT,
                         INDEX idx_product_store_id (store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE product_drop (
                              id BIGINT NOT NULL AUTO_INCREMENT,
                              product_id BIGINT NOT NULL,
                              limited_quantity INT NOT NULL,
                              remaining_quantity INT NOT NULL,
                              max_quantity_per_user INT NOT NULL,
                              start_at DATETIME NOT NULL,
                              end_at DATETIME NOT NULL,
                              status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                              is_returned BOOLEAN NOT NULL DEFAULT FALSE,

                              CONSTRAINT pk_product_drop PRIMARY KEY (id),
                              CONSTRAINT ck_product_drop_remaining_quantity_nonnegative
                                  CHECK (remaining_quantity >= 0),
                              CONSTRAINT fk_product_drop_product
                                  FOREIGN KEY (product_id) REFERENCES product (id)
                                      ON DELETE RESTRICT ON UPDATE RESTRICT,
                              CONSTRAINT ck_product_drop_status
                                  CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
                              INDEX idx_product_drop_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE purchase_order (
                                id BIGINT NOT NULL AUTO_INCREMENT,
                                app_user_id BIGINT NOT NULL,
                                product_drop_id BIGINT NOT NULL,
                                quantity INT NOT NULL,
                                unit_price INT NOT NULL,
                                status VARCHAR(20) NOT NULL DEFAULT 'PAYMENT_PENDING',
                                payment_expires_at DATETIME NOT NULL,
                                created_at DATETIME NOT NULL,

                                CONSTRAINT pk_purchase_order PRIMARY KEY (id),
                                CONSTRAINT fk_purchase_order_app_user
                                    FOREIGN KEY (app_user_id) REFERENCES app_user (id)
                                        ON DELETE RESTRICT ON UPDATE RESTRICT,
                                CONSTRAINT fk_purchase_order_product_drop
                                    FOREIGN KEY (product_drop_id) REFERENCES product_drop (id)
                                        ON DELETE RESTRICT ON UPDATE RESTRICT,
                                CONSTRAINT ck_purchase_order_status
                                    CHECK (status IN ('PAYMENT_PENDING', 'COMPLETED', 'CANCELLED')),
                                INDEX idx_purchase_order_app_user_id (app_user_id),
                                INDEX idx_purchase_order_product_drop_id (product_drop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
