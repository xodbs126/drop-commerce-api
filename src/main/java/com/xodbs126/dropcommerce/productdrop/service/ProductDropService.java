package com.xodbs126.dropcommerce.productdrop.service;

import com.xodbs126.dropcommerce.productdrop.dto.request.ProductDropSearchRequestDto;
import com.xodbs126.dropcommerce.productdrop.dto.response.ProductDropDetailResponseDto;
import com.xodbs126.dropcommerce.productdrop.dto.response.ProductDropListResponseDto;
import com.xodbs126.dropcommerce.productdrop.entity.ProductDrop;
import com.xodbs126.dropcommerce.productdrop.exception.ProductDropException;
import com.xodbs126.dropcommerce.productdrop.repository.ProductDropRepository;
import com.xodbs126.dropcommerce.productdrop.repository.ProductDropSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

    @Transactional(readOnly = true)
    public ProductDropListResponseDto getProductDropList(ProductDropSearchRequestDto requestDto) {
        LocalDateTime now = LocalDateTime.now(clock);
        Pageable pageable = PageRequest.of(requestDto.page(), requestDto.size());
        ProductDropSearchCondition condition = new ProductDropSearchCondition(requestDto.keyword(), requestDto.category(), requestDto.phase(), now);
        Slice<ProductDrop> productDropList = productDropRepository.search(condition, pageable);

        return new ProductDropListResponseDto(productDropList, now);
    }
}
