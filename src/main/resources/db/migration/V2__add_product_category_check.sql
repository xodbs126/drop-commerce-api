ALTER TABLE product
    ADD CONSTRAINT ck_product_category
        CHECK (category IN ('FOOD', 'BOOK', 'TICKET', 'VOUCHER'));
