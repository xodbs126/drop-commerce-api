package com.xodbs126.dropcommerce.product.controller;

import com.xodbs126.dropcommerce.product.dto.ProductRegisterRequestDto;
import com.xodbs126.dropcommerce.product.dto.ProductRegisterResponseDto;
import com.xodbs126.dropcommerce.product.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
