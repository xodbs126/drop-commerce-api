package com.xodbs126.dropcommerce.product.exception;

import com.xodbs126.dropcommerce.global.exception.errorcode.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ProductErrorCode implements ErrorCode {

    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "해당 상품을 찾을 수 없습니다."),
    STOCK_INSUFFICIENT(HttpStatus.CONFLICT, "STOCK_INSUFFICIENT", "해당 상품의 재고가 부족합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
