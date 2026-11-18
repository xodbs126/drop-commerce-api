package com.xodbs126.dropcommerce.productdrop.dto.request;

import com.xodbs126.dropcommerce.product.entity.ProductCategory;
import com.xodbs126.dropcommerce.productdrop.entity.DropPhase;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ProductDropSearchRequestDto(String keyword, ProductCategory category, DropPhase phase,
                                          @Min(0) Integer page, @Min(1) @Max(100) Integer size, String sort) {
    public ProductDropSearchRequestDto {
        if (page == null) {
            page = 0;
        }

        if (size == null) {
            size = 20;
        }
    }
}
