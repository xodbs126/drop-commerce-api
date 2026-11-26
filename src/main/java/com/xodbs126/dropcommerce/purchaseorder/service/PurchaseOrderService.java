package com.xodbs126.dropcommerce.purchaseorder.service;

import com.xodbs126.dropcommerce.productdrop.entity.DropPhase;
import com.xodbs126.dropcommerce.productdrop.entity.ProductDrop;
import com.xodbs126.dropcommerce.productdrop.exception.ProductDropErrorCode;
import com.xodbs126.dropcommerce.productdrop.exception.ProductDropException;
import com.xodbs126.dropcommerce.productdrop.repository.ProductDropRepository;
import com.xodbs126.dropcommerce.purchaseorder.dto.PurchaseOrderRequestDto;
import com.xodbs126.dropcommerce.purchaseorder.dto.PurchaseOrderResponseDto;
import com.xodbs126.dropcommerce.purchaseorder.entity.OrderStatus;
import com.xodbs126.dropcommerce.purchaseorder.entity.PurchaseOrder;
import com.xodbs126.dropcommerce.purchaseorder.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductDropRepository productDropRepository;
    private final Clock clock;

    public PurchaseOrderResponseDto createOrder(Long dropId, Long appUserId, PurchaseOrderRequestDto requestDto) {
        LocalDateTime now = LocalDateTime.now(clock);
        ProductDrop productDrop = productDropRepository.findByIdAndApprovedAtIsNotNull(dropId).orElseThrow(() -> new ProductDropException(ProductDropErrorCode.DROP_NOT_FOUND));
        DropPhase phase = productDrop.getPhase(now);
        if (phase != DropPhase.ONGOING) {
            throw new ProductDropException(ProductDropErrorCode.DROP_NOT_ORDERABLE);
        }

        int cumulativeAmount = (int) purchaseOrderRepository.sumQuantityByAppUserIdAndProductDrop(appUserId, productDrop, OrderStatus.CANCELLED);

        productDrop.decreaseRemainingQuantity(requestDto.quantity(), cumulativeAmount);
        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(new PurchaseOrder(appUserId, productDrop, requestDto.quantity(), now));
        return new PurchaseOrderResponseDto(savedPurchaseOrder.getId(), savedPurchaseOrder.getPaymentExpiresAt());
    }

}
