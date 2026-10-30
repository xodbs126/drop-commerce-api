package com.xodbs126.dropcommerce.global.exception.errorcode;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    String getMessage();
    String getCode();
    HttpStatus getStatus();
}
