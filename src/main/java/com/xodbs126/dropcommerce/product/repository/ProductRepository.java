package com.xodbs126.dropcommerce.product.repository;

import com.xodbs126.dropcommerce.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
