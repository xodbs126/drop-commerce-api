package com.xodbs126.dropcommerce.productdrop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.xodbs126.dropcommerce.product.entity.ProductCategory;
import com.xodbs126.dropcommerce.productdrop.entity.DropPhase;
import com.xodbs126.dropcommerce.productdrop.entity.DropStatus;
import com.xodbs126.dropcommerce.productdrop.entity.ProductDrop;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.xodbs126.dropcommerce.product.entity.QProduct.product;
import static com.xodbs126.dropcommerce.productdrop.entity.QProductDrop.productDrop;
import static com.xodbs126.dropcommerce.store.entity.QStore.store;

@RequiredArgsConstructor
public class ProductDropRepositoryImpl implements ProductDropRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ProductDrop> search(ProductDropSearchCondition condition) {
        return jpaQueryFactory.selectFrom(productDrop)
                .join(productDrop.product, product)
                .join(product.store, store)
                .where(product.deletedAt.isNull(),
                        phaseCondition(condition.phase(), condition.now()),
                        categoryCondition(condition.category()),
                        keywordCondition(condition.keyword()))
                .orderBy(productDrop.startAt.asc(), productDrop.id.asc())
                .fetch();
    }

    private BooleanExpression keywordCondition(String keyword) {
        if (!StringUtils.hasText(keyword)) return null;
        return product.name.contains(keyword)
                .or(product.description.contains(keyword))
                .or(store.name.contains(keyword));
    }

    private BooleanExpression categoryCondition(ProductCategory category) {
        return category != null ? product.category.eq(category) : null;
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
