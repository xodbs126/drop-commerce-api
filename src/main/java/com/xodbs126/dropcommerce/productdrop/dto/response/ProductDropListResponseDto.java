package com.xodbs126.dropcommerce.productdrop.dto.response;

import com.xodbs126.dropcommerce.productdrop.entity.ProductDrop;

import java.time.LocalDateTime;
import java.util.List;

public record ProductDropListResponseDto(List<ProductDropSummaryResponseDto> content, int page, int size, boolean hasNext) {
    public ProductDropListResponseDto(List<ProductDrop> drops, LocalDateTime now, int page, int size, boolean hasNext) {
        this(
                drops.stream()
                        .map(drop -> new ProductDropSummaryResponseDto(drop, now))
                        .toList(),
                page,
                size,
                hasNext
        );
    }
}
