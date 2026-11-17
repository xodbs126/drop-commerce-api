package com.xodbs126.dropcommerce.productdrop.exception;

import com.xodbs126.dropcommerce.global.exception.GlobalException;

public class ProductDropException extends GlobalException {
    public ProductDropException(ProductDropErrorCode productDropErrorCode) {
        super(productDropErrorCode);
    }
}
