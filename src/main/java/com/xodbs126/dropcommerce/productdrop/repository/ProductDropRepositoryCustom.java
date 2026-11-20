package com.xodbs126.dropcommerce.productdrop.repository;

import com.xodbs126.dropcommerce.productdrop.entity.ProductDrop;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ProductDropRepositoryCustom {
    Slice<ProductDrop> search(ProductDropSearchCondition condition, Pageable pageable);
}
