package com.xodbs126.dropcommerce.purchaseorder.dto;

import java.time.LocalDateTime;

public record PurchaseOrderResponseDto(Long orderId, LocalDateTime paymentExpiresAt) {
}
