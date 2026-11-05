package com.xodbs126.dropcommerce.global.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "입력값을 확인해 주세요."),
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "LOGIN_REQUIRED", "로그인이 필요합니다."),
    ADMIN_REQUIRED(HttpStatus.FORBIDDEN, "ADMIN_REQUIRED", "관리자 권한이 필요합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
