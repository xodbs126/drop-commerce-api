package com.xodbs126.dropcommerce.purchaseorder.dto;

import jakarta.validation.constraints.Min;

public record PurchaseOrderRequestDto(@Min(1) int quantity) {
}
