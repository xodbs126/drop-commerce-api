package com.xodbs126.dropcommerce.productdrop.repository;

import com.xodbs126.dropcommerce.productdrop.entity.ProductDrop;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface ProductDropRepositoryCustom {
    List<ProductDrop> search(ProductDropSearchCondition condition);
}
