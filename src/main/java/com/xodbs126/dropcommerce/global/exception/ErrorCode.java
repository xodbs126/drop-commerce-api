package com.xodbs126.dropcommerce.global.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    String getMessage();
    String getCode();
    HttpStatus getStatus();
}
