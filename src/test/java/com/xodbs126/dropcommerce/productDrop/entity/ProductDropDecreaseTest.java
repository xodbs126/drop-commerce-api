package com.xodbs126.dropcommerce.productDrop.entity;

import com.xodbs126.dropcommerce.global.exception.GlobalException;
import com.xodbs126.dropcommerce.global.exception.errorcode.CommonErrorCode;
import com.xodbs126.dropcommerce.productdrop.entity.ProductDrop;
import com.xodbs126.dropcommerce.productdrop.exception.ProductDropErrorCode;
import com.xodbs126.dropcommerce.productdrop.exception.ProductDropException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class ProductDropDecreaseTest {

    @Test
    void 남은_수량만큼_차감된다() {
        //given
        ProductDrop productDrop = new ProductDrop(null, 10, 3, LocalDateTime.now(), LocalDateTime.now());

        //when
        productDrop.decreaseRemainingQuantity(3, 0);

        //then
        assertThat(productDrop.getRemainingQuantity()).isEqualTo(7);
    }

    @Test
    void 남은_수량을_전부_사면_0이_된다() {
        //given
        ProductDrop productDrop = new ProductDrop(null, 3, 3, LocalDateTime.now(), LocalDateTime.now());

        //when
        productDrop.decreaseRemainingQuantity(3, 0);

        //then
        assertThat(productDrop.getRemainingQuantity()).isEqualTo(0);
    }

    @Test
    void 누적_구매가_1인_제한을_넘으면_거부된다() {
        //given
        ProductDrop productDrop = new ProductDrop(null, 10, 5, LocalDateTime.now(), LocalDateTime.now());

        //when //then
        assertThatThrownBy(() -> productDrop.decreaseRemainingQuantity(5, 1))
                .isInstanceOf(ProductDropException.class)
                .extracting("errorCode")
                .isEqualTo(ProductDropErrorCode.PURCHASE_LIMIT_EXCEEDED);
        assertThat(productDrop.getRemainingQuantity()).isEqualTo(10);
    }

    @Test
    void 누적_구매가_1인_제한과_같으면_허용된다() {
        //given
        ProductDrop productDrop = new ProductDrop(null, 10, 5, LocalDateTime.now(), LocalDateTime.now());

        //when
        productDrop.decreaseRemainingQuantity(2, 3);

        //then
        assertThat(productDrop.getRemainingQuantity()).isEqualTo(8);
    }

    @Test
    void 남은_수량보다_많이_사면_거부된다() {
        //given
        ProductDrop productDrop = new ProductDrop(null, 3, 3, LocalDateTime.now(), LocalDateTime.now());
        productDrop.decreaseRemainingQuantity(2, 0);

        //when //then
        assertThatThrownBy(() -> productDrop.decreaseRemainingQuantity(2, 0))
                .isInstanceOf(ProductDropException.class)
                .extracting("errorCode")
                .isEqualTo(ProductDropErrorCode.REMAINING_QUANTITY_INSUFFICIENT);
        assertThat(productDrop.getRemainingQuantity()).isEqualTo(1);
    }

    @Test
    void 수량이_0이면_거부된다() {
        //given
        ProductDrop productDrop = new ProductDrop(null, 5, 3, LocalDateTime.now(), LocalDateTime.now());

        //when //then
        assertThatThrownBy(() -> productDrop.decreaseRemainingQuantity(0, 0))
                .isInstanceOf(GlobalException.class)
                .extracting("errorCode")
                .isEqualTo(CommonErrorCode.INVALID_INPUT);
        assertThat(productDrop.getRemainingQuantity()).isEqualTo(5);
    }

    @Test
    void 수량이_음수면_거부된다() {
        //given
        ProductDrop productDrop = new ProductDrop(null, 5, 3, LocalDateTime.now(), LocalDateTime.now());

        //when //then
        assertThatThrownBy(() -> productDrop.decreaseRemainingQuantity(-1, 0))
                .isInstanceOf(GlobalException.class)
                .extracting("errorCode")
                .isEqualTo(CommonErrorCode.INVALID_INPUT);
        assertThat(productDrop.getRemainingQuantity()).isEqualTo(5);
    }
}
