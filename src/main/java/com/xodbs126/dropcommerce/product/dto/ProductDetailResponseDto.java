package com.xodbs126.dropcommerce.product.dto;

import com.xodbs126.dropcommerce.product.entity.ProductCategory;

public record ProductDetailResponseDto(Long productId, String name, String description, ProductCategory category,
                                       int price, int stock) {
}
