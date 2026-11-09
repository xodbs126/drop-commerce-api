package com.xodbs126.dropcommerce.store.service;

import com.xodbs126.dropcommerce.store.entity.Store;
import com.xodbs126.dropcommerce.store.exception.StoreException;
import com.xodbs126.dropcommerce.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.xodbs126.dropcommerce.store.exception.StoreErrorCode.STORE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    public Store findByAppUserId(Long appUserId) {
        return storeRepository.findStoreByOwnerAppUserId(appUserId)
                .orElseThrow(() -> new StoreException(STORE_NOT_FOUND));
    }
}
