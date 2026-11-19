package com.xodbs126.dropcommerce.productdrop.controller;

import com.xodbs126.dropcommerce.productdrop.dto.request.ProductDropSearchRequestDto;
import com.xodbs126.dropcommerce.productdrop.dto.response.ProductDropDetailResponseDto;
import com.xodbs126.dropcommerce.productdrop.dto.response.ProductDropListResponseDto;
import com.xodbs126.dropcommerce.productdrop.service.ProductDropService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductDropController {

    private final ProductDropService productDropService;

    @GetMapping("/drops/{dropId}")
    public ResponseEntity<ProductDropDetailResponseDto> getProductDropDetail(@PathVariable Long dropId) {
        ProductDropDetailResponseDto responseDto = productDropService.getProductDropDetail(dropId);

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @GetMapping("/drops")
    public ResponseEntity<ProductDropListResponseDto> getProductDropList(@ModelAttribute @Valid ProductDropSearchRequestDto requestDto) {
        ProductDropListResponseDto responseDtos = productDropService.getProductDropList(requestDto);

        return ResponseEntity.status(HttpStatus.OK).body(responseDtos);
    }
}
