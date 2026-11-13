package com.xodbs126.dropcommerce.product.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ProductListRequestDto(@Min(0) Integer page, @Min(1) @Max(100) Integer size) {
    public ProductListRequestDto {
        if (page == null) {
            page = 0;
        }

        if (size == null) {
            size = 20;
        }
    }
}
