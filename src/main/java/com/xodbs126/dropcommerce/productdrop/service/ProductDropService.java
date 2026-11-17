package com.xodbs126.dropcommerce.productdrop.service;

import com.xodbs126.dropcommerce.productdrop.dto.response.ProductDropDetailResponseDto;
import com.xodbs126.dropcommerce.productdrop.entity.ProductDrop;
import com.xodbs126.dropcommerce.productdrop.exception.ProductDropException;
import com.xodbs126.dropcommerce.productdrop.repository.ProductDropRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

import static com.xodbs126.dropcommerce.productdrop.exception.ProductDropErrorCode.DROP_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductDropService {

    private final ProductDropRepository productDropRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public ProductDropDetailResponseDto getProductDropDetail(Long dropId) {
        ProductDrop drop = productDropRepository.findByIdAndApprovedAtIsNotNull(dropId).orElseThrow(() -> new ProductDropException(DROP_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now(clock);

        return new ProductDropDetailResponseDto(drop, now);
    }
}
