package com.xodbs126.dropcommerce.product.dto;

import com.xodbs126.dropcommerce.product.entity.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProductRegisterRequestDto(@NotBlank @Size(max = 150) String name,
                                        @NotNull String description,
                                        @NotNull ProductCategory category,
                                        @Positive int price) {

}
