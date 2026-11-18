package com.xodbs126.dropcommerce.productdrop.dto.response;

import com.xodbs126.dropcommerce.product.entity.ProductCategory;
import com.xodbs126.dropcommerce.productdrop.entity.DropPhase;
import com.xodbs126.dropcommerce.productdrop.entity.DropStatus;
import com.xodbs126.dropcommerce.productdrop.entity.ProductDrop;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

public record ProductDropSummaryResponseDto(Long dropId, String productName, ProductCategory category,
                                            int price, String storeName,
                                            LocalDateTime startAt, LocalDateTime endAt, DropPhase phase,
                                            DropStatus status, int limitedQuantity, int remainingQuantity,
                                            int maxQuantityPerUser) {

    public ProductDropSummaryResponseDto(ProductDrop drop, LocalDateTime now) {
        this(
                drop.getId(),
                drop.getProduct().getName(),
                drop.getProduct().getCategory(),
                drop.getProduct().getPrice(),
                drop.getProduct().getStore().getName(),
                drop.getStartAt(),
                drop.getEndAt(),
                drop.getPhase(now),
                drop.getStatus(),
                drop.getLimitedQuantity(),
                drop.getRemainingQuantity(),
                drop.getMaxQuantityPerUser()
        );
    }
}
