package com.xodbs126.dropcommerce.product.repository;

import com.xodbs126.dropcommerce.product.entity.Product;
import com.xodbs126.dropcommerce.store.entity.Store;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndStoreAndDeletedAtIsNull(Long productId, Store findStore);

    Slice<Product> findAllByStoreAndDeletedAtIsNull(Store findStore, Pageable pageable);
}
