package com.xodbs126.dropcommerce.store.exception;

import com.xodbs126.dropcommerce.global.exception.GlobalException;

public class StoreException extends GlobalException {
    public StoreException(StoreErrorCode errorCode) {
        super(errorCode);
    }
}
