package com.xodbs126.dropcommerce.purchaseOrder;


import com.xodbs126.dropcommerce.IntegrationTest;
import com.xodbs126.dropcommerce.appuser.repository.AppUserRepository;
import com.xodbs126.dropcommerce.product.repository.ProductRepository;
import com.xodbs126.dropcommerce.productdrop.repository.ProductDropRepository;
import com.xodbs126.dropcommerce.purchaseorder.repository.PurchaseOrderRepository;
import com.xodbs126.dropcommerce.store.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.convention.TestBean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@AutoConfigureMockMvc
public class PurchaseOrderCreateTest extends IntegrationTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductDropRepository productDropRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @TestBean
    private Clock clock;

    static Clock clock() {
        return Clock.fixed(Instant.parse("2026-11-19T02:00:00Z"), ZoneId.of("Asia/Seoul"));
    }


}
