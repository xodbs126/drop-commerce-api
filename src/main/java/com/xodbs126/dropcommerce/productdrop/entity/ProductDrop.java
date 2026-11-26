package com.xodbs126.dropcommerce.productdrop.entity;

import com.xodbs126.dropcommerce.global.exception.GlobalException;
import com.xodbs126.dropcommerce.global.exception.errorcode.CommonErrorCode;
import com.xodbs126.dropcommerce.product.entity.Product;
import com.xodbs126.dropcommerce.productdrop.exception.ProductDropErrorCode;
import com.xodbs126.dropcommerce.productdrop.exception.ProductDropException;
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

    public DropPhase getPhase(LocalDateTime now) {
        if (this.status == DropStatus.CANCELLED) {
            return null;
        }
        if (now.isBefore(this.startAt)) {
            return DropPhase.UPCOMING;
        } else if (now.isBefore(this.endAt)) {
            return DropPhase.ONGOING;
        } else {
            return DropPhase.ENDED;
        }
    }

    public void decreaseRemainingQuantity(int amount, int cumulativeAmount) {
        if (amount <= 0) {
            throw new GlobalException(CommonErrorCode.INVALID_INPUT);
        }
        if (cumulativeAmount + amount > maxQuantityPerUser) {
            throw new ProductDropException(ProductDropErrorCode.PURCHASE_LIMIT_EXCEEDED);
        }
        if (remainingQuantity - amount < 0) {
            throw new ProductDropException(ProductDropErrorCode.REMAINING_QUANTITY_INSUFFICIENT);
        }
        this.remainingQuantity -= amount;
    }
}
