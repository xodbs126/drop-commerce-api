
package com.xodbs126.dropcommerce.purchaseorder.entity;

import com.xodbs126.dropcommerce.productdrop.entity.ProductDrop;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PurchaseOrder {

    private static final Duration PAYMENT_TIMEOUT =
            Duration.ofMinutes(10);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long appUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_drop_id")
    private ProductDrop productDrop;

    private int quantity;

    private int unitPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime paymentExpiresAt;

    private LocalDateTime createdAt;

    public PurchaseOrder(
            Long appUserId, ProductDrop productDrop, int quantity, LocalDateTime createdAt) {
        this.appUserId = appUserId;
        this.productDrop = productDrop;
        this.quantity = quantity;

        this.unitPrice = productDrop.getProduct().getPrice();
        this.status = OrderStatus.PAYMENT_PENDING;

        this.createdAt = createdAt;
        this.paymentExpiresAt = createdAt.plus(PAYMENT_TIMEOUT);
    }
}
