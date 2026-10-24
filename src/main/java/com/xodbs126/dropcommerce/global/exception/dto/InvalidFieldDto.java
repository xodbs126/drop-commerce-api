package com.xodbs126.dropcommerce.global.exception.dto;

import com.xodbs126.dropcommerce.global.exception.errorcode.Reason;

public record InvalidFieldDto(String field, Reason reason) {
}
