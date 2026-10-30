package com.xodbs126.dropcommerce.appuser.exception;

import com.xodbs126.dropcommerce.global.exception.GlobalException;

public class AppUserException extends GlobalException {

    public AppUserException(AppUserErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}