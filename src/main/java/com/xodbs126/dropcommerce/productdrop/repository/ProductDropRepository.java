package com.xodbs126.dropcommerce.productdrop.repository;

import com.xodbs126.dropcommerce.productdrop.entity.ProductDrop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductDropRepository extends JpaRepository<ProductDrop, Long> {
    Optional<ProductDrop> findByIdAndApprovedAtIsNotNull(Long id);
}
