package com.xodbs126.dropcommerce.product.exception;

import com.xodbs126.dropcommerce.global.exception.GlobalException;

public class ProductException extends GlobalException {
    public ProductException(ProductErrorCode productErrorCode) {
        super(productErrorCode);
    }
}
