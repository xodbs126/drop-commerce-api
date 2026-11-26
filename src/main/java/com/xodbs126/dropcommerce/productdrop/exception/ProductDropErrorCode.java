package com.xodbs126.dropcommerce.productdrop.exception;

import com.xodbs126.dropcommerce.global.exception.errorcode.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductDropErrorCode implements ErrorCode {

    DROP_NOT_FOUND(HttpStatus.NOT_FOUND, "DROP_NOT_FOUND", "해당 드롭을 찾을 수 없습니다."),
    REMAINING_QUANTITY_INSUFFICIENT(HttpStatus.CONFLICT, "REMAINING_QUANTITY_INSUFFICIENT", "해당 드롭의 남은 수량이 부족합니다."),
    PURCHASE_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "PURCHASE_LIMIT_EXCEEDED", "1인당 구매 제한을 초과했습니다."),
    DROP_NOT_ORDERABLE(HttpStatus.CONFLICT, "DROP_NOT_ORDERABLE", "현재 구매 할 수 없는 드롭입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
