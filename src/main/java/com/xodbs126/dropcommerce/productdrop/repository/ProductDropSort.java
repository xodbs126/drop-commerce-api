package com.xodbs126.dropcommerce.productdrop.repository;

import org.springframework.data.domain.Sort;

public record ProductDropSort(DropSortField field, Sort.Direction direction) {
}
