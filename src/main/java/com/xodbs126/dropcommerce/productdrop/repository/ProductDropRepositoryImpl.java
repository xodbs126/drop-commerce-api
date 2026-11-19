package com.xodbs126.dropcommerce.productdrop.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.xodbs126.dropcommerce.product.entity.QProduct;
import com.xodbs126.dropcommerce.productdrop.entity.DropPhase;
import com.xodbs126.dropcommerce.productdrop.entity.DropStatus;
import com.xodbs126.dropcommerce.productdrop.entity.ProductDrop;
import com.xodbs126.dropcommerce.productdrop.entity.QProductDrop;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
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
                .where(product.deletedAt.isNull(),
                        phaseCondition(condition.phase(),condition.now()))
                .fetch();
    }

    private BooleanExpression phaseCondition(DropPhase phase, LocalDateTime now) {
        return switch (phase) {
            case null -> productDrop.approvedAt.isNotNull().and(productDrop.endAt.gt(now));
            case UPCOMING -> productDrop.status.eq(DropStatus.APPROVED)
                    .and(productDrop.startAt.gt(now));
            case ONGOING -> productDrop.status.eq(DropStatus.APPROVED)
                    .and(productDrop.startAt.loe(now))
                    .and(productDrop.endAt.gt(now));
            case ENDED -> productDrop.approvedAt.isNotNull()
                    .and(productDrop.endAt.loe(now));
        };
    }
}
