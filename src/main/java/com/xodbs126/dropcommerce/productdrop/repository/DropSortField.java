package com.xodbs126.dropcommerce.productdrop.repository;

import com.xodbs126.dropcommerce.global.exception.GlobalException;
import com.xodbs126.dropcommerce.global.exception.errorcode.CommonErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DropSortField {
    PRICE("price"), START_AT("startAt"), LIMITED_QUANTITY("limitedQuantity"), REMAINING_QUANTITY("remainingQuantity");

    private final String fieldName;

    public static DropSortField from(String fieldName) {
        for (DropSortField field : DropSortField.values()) {
            if (field.getFieldName().equals(fieldName)) {
                return field;
            }
        }
        throw new GlobalException(CommonErrorCode.INVALID_INPUT);
    }
}
