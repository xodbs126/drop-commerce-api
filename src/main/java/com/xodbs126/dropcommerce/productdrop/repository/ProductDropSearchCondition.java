package com.xodbs126.dropcommerce.productdrop.repository;

import com.xodbs126.dropcommerce.product.entity.ProductCategory;
import com.xodbs126.dropcommerce.productdrop.entity.DropPhase;

import java.time.LocalDateTime;

public record ProductDropSearchCondition(
        String keyword,
        ProductCategory category,
        DropPhase phase,
        LocalDateTime now
) {
}
