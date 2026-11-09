package com.xodbs126.dropcommerce.store.repository;

import com.xodbs126.dropcommerce.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findStoreByOwnerAppUserId(Long ownerAppUserId);
}
