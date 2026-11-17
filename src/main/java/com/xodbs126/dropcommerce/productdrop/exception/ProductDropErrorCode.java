package com.xodbs126.dropcommerce.productdrop.exception;

import com.xodbs126.dropcommerce.global.exception.errorcode.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductDropErrorCode implements ErrorCode {

    DROP_NOT_FOUND(HttpStatus.NOT_FOUND, "DROP_NOT_FOUND", "해당 드롭을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
