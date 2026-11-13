package com.xodbs126.dropcommerce.product.dto;

import java.util.List;

public record ProductListResponseDto(List<ProductDetailResponseDto> content, int page, int size, boolean hasNext) {
}
