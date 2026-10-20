package com.xodbs126.dropcommerce.product.entity;

import com.xodbs126.dropcommerce.store.entity.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "store_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Store store;

    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    private int price;

    private int stock;

    private LocalDateTime deletedAt;

    public Product(Store store, String name, String description, int price, ProductCategory category) {
        this.store = store;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stock = 0;
    }
}
