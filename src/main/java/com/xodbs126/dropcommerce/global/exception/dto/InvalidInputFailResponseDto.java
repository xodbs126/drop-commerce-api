package com.xodbs126.dropcommerce.global.exception.dto;

import java.util.List;

public record InvalidInputFailResponseDto(
        String code,
        String message,
        List<InvalidFieldDto> errors
) {
}