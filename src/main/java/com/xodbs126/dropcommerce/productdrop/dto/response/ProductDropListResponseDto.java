package com.xodbs126.dropcommerce.productdrop.dto.response;

import com.xodbs126.dropcommerce.productdrop.entity.ProductDrop;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;

public record ProductDropListResponseDto(List<ProductDropSummaryResponseDto> content, int page, int size, boolean hasNext) {
    public ProductDropListResponseDto(Slice<ProductDrop> drops, LocalDateTime now) {
        this(
                drops.stream()
                        .map(drop -> new ProductDropSummaryResponseDto(drop, now))
                        .toList(),
                drops.getPageable().getPageNumber(),
                drops.getPageable().getPageSize(),
                drops.hasNext()
        );
    }
}
