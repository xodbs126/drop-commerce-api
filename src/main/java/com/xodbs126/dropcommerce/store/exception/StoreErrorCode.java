package com.xodbs126.dropcommerce.store.exception;

import com.xodbs126.dropcommerce.global.exception.errorcode.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StoreErrorCode implements ErrorCode {

    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE_NOT_FOUND", "해당 상점을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
