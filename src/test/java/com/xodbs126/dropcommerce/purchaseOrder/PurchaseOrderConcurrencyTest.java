package com.xodbs126.dropcommerce.purchaseOrder;

import com.xodbs126.dropcommerce.IntegrationTest;
import com.xodbs126.dropcommerce.appuser.entity.AppUser;
import com.xodbs126.dropcommerce.appuser.repository.AppUserRepository;
import com.xodbs126.dropcommerce.global.exception.GlobalException;
import com.xodbs126.dropcommerce.product.entity.Product;
import com.xodbs126.dropcommerce.product.entity.ProductCategory;
import com.xodbs126.dropcommerce.product.repository.ProductRepository;
import com.xodbs126.dropcommerce.productdrop.entity.ProductDrop;
import com.xodbs126.dropcommerce.productdrop.repository.ProductDropRepository;
import com.xodbs126.dropcommerce.purchaseorder.dto.PurchaseOrderRequestDto;
import com.xodbs126.dropcommerce.purchaseorder.service.PurchaseOrderService;
import com.xodbs126.dropcommerce.store.entity.Store;
import com.xodbs126.dropcommerce.store.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class PurchaseOrderConcurrencyTest extends IntegrationTest {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductDropRepository productDropRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Clock clock;

    @Test
    void 한정수량보다_많은_구매자가_동시에_주문해도_한정수량만큼만_주문된다() throws InterruptedException {
        //given
        int limitedQuantity = 10;
        int buyerCount = 100;
        LocalDateTime now = LocalDateTime.now(clock);

        AppUser owner = appUserRepository.save(new AppUser("owner", "password"));
        Store store = storeRepository.save(new Store(owner.getId(), "testStore"));
        Product product = productRepository.save(new Product(store, "testProduct", "testDescription", 1000, ProductCategory.FOOD));
        ProductDrop drop = productDropRepository.save(new ProductDrop(product, limitedQuantity, 1, now.minusMinutes(10), now.plusMinutes(10)));
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", drop.getId());

        List<Long> buyerIds = new ArrayList<>();
        for (int i = 0; i < buyerCount; i++) {
            buyerIds.add(appUserRepository.save(new AppUser("buyer" + i, "password")).getId());
        }

        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(buyerCount);
        AtomicInteger success = new AtomicInteger();
        Map<String, Integer> failReasons = new ConcurrentHashMap<>();

        //when
        for (Long buyerId : buyerIds) {
            executor.submit(() -> {
                try {
                    start.await();
                    purchaseOrderService.createOrder(drop.getId(), buyerId, new PurchaseOrderRequestDto(1));
                    success.incrementAndGet();
                } catch (GlobalException e) {
                    failReasons.merge(e.getErrorCode().getCode(), 1, Integer::sum);
                } catch (Exception e) {
                    failReasons.merge(e.getClass().getSimpleName(), 1, Integer::sum);
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        boolean finished = done.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        //then
        Integer remainingQuantity = jdbcTemplate.queryForObject(
                "SELECT remaining_quantity FROM product_drop WHERE id = ?", Integer.class, drop.getId());
        Integer orderedQuantity = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(quantity), 0) FROM purchase_order WHERE product_drop_id = ?", Integer.class, drop.getId());

        System.out.printf("[동시 주문] 성공 %d건 / 실패 %s / 주문 수량 합 %d / 남은 수량 %d%n",
                success.get(), failReasons, orderedQuantity, remainingQuantity);

        assertThat(finished).isTrue();
        assertThat(orderedQuantity).isEqualTo(limitedQuantity);
        assertThat(remainingQuantity).isZero();
    }
}
