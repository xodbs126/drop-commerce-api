package com.xodbs126.dropcommerce.product.controller;

import com.xodbs126.dropcommerce.product.dto.request.ProductListRequestDto;
import com.xodbs126.dropcommerce.product.dto.request.ProductRegisterRequestDto;
import com.xodbs126.dropcommerce.product.dto.response.ProductDetailResponseDto;
import com.xodbs126.dropcommerce.product.dto.response.ProductListResponseDto;
import com.xodbs126.dropcommerce.product.dto.response.ProductRegisterResponseDto;
import com.xodbs126.dropcommerce.product.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.xodbs126.dropcommerce.global.session.SessionConst.SESSION_KEY_APP_USER_ID;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/stores/me/products")
    public ResponseEntity<ProductRegisterResponseDto> registerProduct(
            @RequestBody @Valid ProductRegisterRequestDto registerRequestDto,
            HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Long appUserId = (Long) session.getAttribute(SESSION_KEY_APP_USER_ID);

        Long productId = productService.register(registerRequestDto, appUserId);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ProductRegisterResponseDto(productId));
    }

    @GetMapping("/stores/me/products/{productId}")
    public ResponseEntity<ProductDetailResponseDto> getProductDetail(HttpServletRequest request, @PathVariable Long productId) {
        HttpSession session = request.getSession(false);
        Long appUserId = (Long) session.getAttribute(SESSION_KEY_APP_USER_ID);

        ProductDetailResponseDto responseDto = productService.getProduct(appUserId, productId);

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @GetMapping("/stores/me/products")
    public ResponseEntity<ProductListResponseDto> getProductList(
            HttpServletRequest request,
            @ModelAttribute @Valid ProductListRequestDto requestDto) {
        HttpSession session = request.getSession(false);
        Long appUserId = (Long) session.getAttribute(SESSION_KEY_APP_USER_ID);

        ProductListResponseDto responseDto = productService.getProductList(appUserId, requestDto);

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }
}
