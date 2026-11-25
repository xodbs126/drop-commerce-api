package com.xodbs126.dropcommerce.purchaseorder.repository;

import com.xodbs126.dropcommerce.productdrop.entity.ProductDrop;
import com.xodbs126.dropcommerce.purchaseorder.entity.OrderStatus;
import com.xodbs126.dropcommerce.purchaseorder.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Query("SELECT COALESCE(SUM(o.quantity), 0L) FROM PurchaseOrder o " +
            "WHERE o.appUserId = :appUserId " +
            "AND o.productDrop = :productDrop " +
            "AND o.status <> :orderStatus")
    long sumQuantityByAppUserIdAndProductDrop(@Param("appUserId") Long appUserId,
                                              @Param("productDrop") ProductDrop productDrop,
                                              @Param("orderStatus") OrderStatus orderStatus);
}
