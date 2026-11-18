package com.xodbs126.dropcommerce.productdrop.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.xodbs126.dropcommerce.product.entity.QProduct;
import com.xodbs126.dropcommerce.productdrop.entity.ProductDrop;
import com.xodbs126.dropcommerce.productdrop.entity.QProductDrop;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.xodbs126.dropcommerce.product.entity.QProduct.*;
import static com.xodbs126.dropcommerce.productdrop.entity.QProductDrop.*;

@RequiredArgsConstructor
public class ProductDropRepositoryImpl implements ProductDropRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ProductDrop> search(ProductDropSearchCondition condition) {
        return jpaQueryFactory.selectFrom(productDrop)
                .join(productDrop.product, product)
                .where(product.deletedAt.isNull())
                .fetch();
    }
}
