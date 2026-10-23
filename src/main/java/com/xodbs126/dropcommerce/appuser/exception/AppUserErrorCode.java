package com.xodbs126.dropcommerce.appuser.exception;

import com.xodbs126.dropcommerce.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AppUserErrorCode implements ErrorCode {

    USERNAME_DUPLICATED(HttpStatus.CONFLICT, "USERNAME_DUPLICATED", "이미 존재하는 아이디입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
