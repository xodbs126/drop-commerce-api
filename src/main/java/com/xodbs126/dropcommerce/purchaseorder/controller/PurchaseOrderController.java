package com.xodbs126.dropcommerce.purchaseorder.controller;

import com.xodbs126.dropcommerce.purchaseorder.dto.PurchaseOrderRequestDto;
import com.xodbs126.dropcommerce.purchaseorder.dto.PurchaseOrderResponseDto;
import com.xodbs126.dropcommerce.purchaseorder.service.PurchaseOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.xodbs126.dropcommerce.global.session.SessionConst.SESSION_KEY_APP_USER_ID;

@RestController
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping("/drops/{dropId}/orders")
    public ResponseEntity<PurchaseOrderResponseDto> createOrder(HttpServletRequest request,
                                                                @RequestBody @Valid PurchaseOrderRequestDto requestDto,
                                                                @PathVariable Long dropId) {
        Long appUserId = (Long) request.getSession(false).getAttribute(SESSION_KEY_APP_USER_ID);

        PurchaseOrderResponseDto responseDto = purchaseOrderService.createOrder(dropId, appUserId, requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
