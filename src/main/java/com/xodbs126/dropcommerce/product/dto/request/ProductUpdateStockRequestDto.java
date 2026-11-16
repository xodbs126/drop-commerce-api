package com.xodbs126.dropcommerce.product.dto.request;

import com.xodbs126.dropcommerce.global.validation.NonZero;
import jakarta.validation.constraints.NotNull;

public record ProductUpdateStockRequestDto(@NotNull @NonZero Integer delta) {

}
