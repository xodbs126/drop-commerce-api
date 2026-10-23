package com.xodbs126.dropcommerce.appuser.exception;

import lombok.Getter;

@Getter
public class AppUserException extends RuntimeException{

    private final AppUserErrorCode appUserErrorCode;

    public AppUserException( AppUserErrorCode appUserErrorCode,Throwable cause) {
        super(appUserErrorCode.getMessage(), cause);
        this.appUserErrorCode = appUserErrorCode;
    }
}
