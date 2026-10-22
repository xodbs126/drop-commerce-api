package com.xodbs126.dropcommerce.productdrop.entity;

import com.xodbs126.dropcommerce.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductDrop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private int limitedQuantity;

    private int remainingQuantity;

    private int maxQuantityPerUser;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private LocalDateTime approvedAt;

    @Enumerated(EnumType.STRING)
    private DropStatus status;

    private boolean isReturned;

    public ProductDrop(Product product, int limitedQuantity, int maxQuantityPerUser, LocalDateTime startAt, LocalDateTime endAt) {
        this.product = product;
        this.limitedQuantity = limitedQuantity;
        this.remainingQuantity = limitedQuantity;
        this.maxQuantityPerUser = maxQuantityPerUser;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = DropStatus.PENDING;
        this.isReturned = false;
    }
}
