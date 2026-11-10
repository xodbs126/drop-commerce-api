package com.xodbs126.dropcommerce.product.service;

import com.xodbs126.dropcommerce.product.dto.ProductRegisterRequestDto;
import com.xodbs126.dropcommerce.product.entity.Product;
import com.xodbs126.dropcommerce.product.repository.ProductRepository;
import com.xodbs126.dropcommerce.store.entity.Store;
import com.xodbs126.dropcommerce.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final StoreService storeService;

    public Long register(ProductRegisterRequestDto registerRequestDto, Long appUserId) {
        Store findStore = storeService.findByAppUserId(appUserId);

        Product product = new Product(findStore, registerRequestDto.name(), registerRequestDto.description(),
                registerRequestDto.price(), registerRequestDto.category());

        Product savedProduct = productRepository.save(product);

        return savedProduct.getId();
    }
}
