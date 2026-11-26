package com.xodbs126.dropcommerce.purchaseOrder;

import com.jayway.jsonpath.JsonPath;
import com.xodbs126.dropcommerce.IntegrationTest;
import com.xodbs126.dropcommerce.appuser.entity.AppUser;
import com.xodbs126.dropcommerce.appuser.repository.AppUserRepository;
import com.xodbs126.dropcommerce.product.entity.Product;
import com.xodbs126.dropcommerce.product.entity.ProductCategory;
import com.xodbs126.dropcommerce.product.repository.ProductRepository;
import com.xodbs126.dropcommerce.productdrop.entity.ProductDrop;
import com.xodbs126.dropcommerce.productdrop.repository.ProductDropRepository;
import com.xodbs126.dropcommerce.purchaseorder.entity.OrderStatus;
import com.xodbs126.dropcommerce.purchaseorder.entity.PurchaseOrder;
import com.xodbs126.dropcommerce.purchaseorder.repository.PurchaseOrderRepository;
import com.xodbs126.dropcommerce.store.entity.Store;
import com.xodbs126.dropcommerce.store.repository.StoreRepository;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @TestBean
    private Clock clock;

    static Clock clock() {
        return Clock.fixed(Instant.parse("2026-11-19T02:00:00Z"), ZoneId.of("Asia/Seoul"));
    }

    @Test
    void 진행_중인_드롭을_주문하면_결제_대기_주문이_생긴다() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        ProductDrop savedDrop = saveDrop(savedProduct, 10, 2, now.minusMinutes(10), now.plusMinutes(10));
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop.getId());

        MockHttpSession session = login(savedUser.getUsername());

        //when
        MvcResult mvcResult = mockMvc.perform(post("/drops/{dropId}/orders", savedDrop.getId())
                        .session(session)
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "quantity" : 1
                                }
                                """))
                .andDo(print())
                //then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentExpiresAt").value("2026-11-19T11:10:00"))
                .andExpect(jsonPath("$.orderId").isNumber())
                .andReturn();

        Long orderId = JsonPath.parse(mvcResult.getResponse().getContentAsString()).read("$.orderId", Long.class);

        List<PurchaseOrder> orders = purchaseOrderRepository.findAll();
        assertThat(orders).hasSize(1);
        PurchaseOrder order = orders.getFirst();
        assertThat(order.getId()).isEqualTo(orderId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        assertThat(order.getUnitPrice()).isEqualTo(1000);
        assertThat(order.getQuantity()).isEqualTo(1);
        assertThat(order.getAppUserId()).isEqualTo(savedUser.getId());
        Integer result = jdbcTemplate.queryForObject("SELECT remaining_quantity FROM product_drop WHERE id = ?", Integer.class, savedDrop.getId());
        assertThat(result).isEqualTo(9);
    }

    @Test
    void 기존_주문과_합쳐_1인_제한을_넘으면_거부된다() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        ProductDrop savedDrop = saveDrop(savedProduct, 10, 3, now.minusMinutes(10), now.plusMinutes(10));
        setLastOrder(savedUser, savedDrop, 2, now);

        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop.getId());

        MockHttpSession session = login(savedUser.getUsername());

        //when
        mockMvc.perform(post("/drops/{dropId}/orders", savedDrop.getId())
                        .session(session)
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "quantity" : 2
                                }
                                """))
                .andDo(print())
                //then
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PURCHASE_LIMIT_EXCEEDED"));

        List<PurchaseOrder> orders = purchaseOrderRepository.findAll();
        assertThat(orders).hasSize(1);
        Integer remainingQuantity = jdbcTemplate.queryForObject("SELECT remaining_quantity FROM product_drop WHERE id = ?", Integer.class, savedDrop.getId());
        assertThat(remainingQuantity).isEqualTo(10);
    }

    @Test
    void 취소된_주문은_누적_구매_수량에서_빠진다() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        ProductDrop savedDrop = saveDrop(savedProduct, 10, 3, now.minusMinutes(10), now.plusMinutes(10));
        PurchaseOrder lastOrder1 = setLastOrder(savedUser, savedDrop, 1, now);
        PurchaseOrder lastOrder2 = setLastOrder(savedUser, savedDrop, 1, now);

        jdbcTemplate.update("UPDATE purchase_order SET status = ? WHERE id IN (?, ?)", "CANCELLED", lastOrder1.getId(), lastOrder2.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop.getId());

        MockHttpSession session = login(savedUser.getUsername());

        //when
        mockMvc.perform(post("/drops/{dropId}/orders", savedDrop.getId())
                        .header("Origin", "http://localhost:3000")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "quantity" : 3
                                }
                                """))
                .andDo(print())
                //then
                .andExpect(status().isCreated());

        List<PurchaseOrder> orders = purchaseOrderRepository.findAll();
        assertThat(orders).hasSize(3);
        Integer remainingQuantity = jdbcTemplate.queryForObject("SELECT remaining_quantity FROM product_drop WHERE id = ?", Integer.class, savedDrop.getId());
        assertThat(remainingQuantity).isEqualTo(7);
    }

    @Test
    void 다른_회원의_주문은_누적_구매_수량에_들어가지_않는다() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        AppUser otherUser = saveUser("testuser321");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        ProductDrop savedDrop = saveDrop(savedProduct, 10, 3, now.minusMinutes(10), now.plusMinutes(10));
        setLastOrder(otherUser, savedDrop, 3, now);

        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop.getId());

        MockHttpSession session = login(savedUser.getUsername());

        //when
        mockMvc.perform(post("/drops/{dropId}/orders", savedDrop.getId())
                        .session(session)
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "quantity" : 3
                                }
                                """))
                .andDo(print())
                //then
                .andExpect(status().isCreated());

        List<PurchaseOrder> orders = purchaseOrderRepository.findAll();
        assertThat(orders).hasSize(2);
        Integer remainingQuantity = jdbcTemplate.queryForObject("SELECT remaining_quantity FROM product_drop WHERE id = ?", Integer.class, savedDrop.getId());
        assertThat(remainingQuantity).isEqualTo(7);
    }

    @Test
    void 남은_수량보다_많이_주문하면_거부된다() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        ProductDrop savedDrop = saveDrop(savedProduct, 2, 5, now.minusMinutes(10), now.plusMinutes(10));

        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop.getId());

        MockHttpSession session = login(savedUser.getUsername());

        //when
        mockMvc.perform(post("/drops/{dropId}/orders", savedDrop.getId())
                        .session(session)
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "quantity" : 3
                                }
                                """))
                .andDo(print())
                //then
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REMAINING_QUANTITY_INSUFFICIENT"));

        assertThat(purchaseOrderRepository.findAll()).isEmpty();
        Integer remainingQuantity = jdbcTemplate.queryForObject("SELECT remaining_quantity FROM product_drop WHERE id = ?", Integer.class, savedDrop.getId());
        assertThat(remainingQuantity).isEqualTo(2);
    }

    @Test
    void 예정_드롭은_주문이_거부된다() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        ProductDrop savedDrop = saveDrop(savedProduct, 10, 3, now.plusMinutes(10), now.plusMinutes(20));

        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop.getId());

        MockHttpSession session = login(savedUser.getUsername());

        //when
        mockMvc.perform(post("/drops/{dropId}/orders", savedDrop.getId())
                        .session(session)
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "quantity" : 1
                                }
                                """))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DROP_NOT_ORDERABLE"));

        assertThat(purchaseOrderRepository.findAll()).isEmpty();
        Integer remainingQuantity = jdbcTemplate.queryForObject("SELECT remaining_quantity FROM product_drop WHERE id = ?", Integer.class, savedDrop.getId());
        assertThat(remainingQuantity).isEqualTo(10);
    }

    @Test
    void 종료된_드롭은_주문이_거부된다() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        ProductDrop savedDrop = saveDrop(savedProduct, 10, 3, now.minusMinutes(20), now.minusMinutes(10));

        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop.getId());

        MockHttpSession session = login(savedUser.getUsername());

        //when
        mockMvc.perform(post("/drops/{dropId}/orders", savedDrop.getId())
                        .session(session)
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "quantity" : 1
                                }
                                """))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DROP_NOT_ORDERABLE"));

        assertThat(purchaseOrderRepository.findAll()).isEmpty();
        Integer remainingQuantity = jdbcTemplate.queryForObject("SELECT remaining_quantity FROM product_drop WHERE id = ?", Integer.class, savedDrop.getId());
        assertThat(remainingQuantity).isEqualTo(10);
    }

    @Test
    void 시작_시각과_같으면_주문된다() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        ProductDrop savedDrop = saveDrop(savedProduct, 10, 3, now, now.plusMinutes(10));

        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop.getId());

        MockHttpSession session = login(savedUser.getUsername());

        //when
        mockMvc.perform(post("/drops/{dropId}/orders", savedDrop.getId())
                        .session(session)
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "quantity" : 1
                                }
                                """))
                .andDo(print())
                .andExpect(status().isCreated());

        assertThat(purchaseOrderRepository.findAll()).hasSize(1);
        Integer remainingQuantity = jdbcTemplate.queryForObject("SELECT remaining_quantity FROM product_drop WHERE id = ?", Integer.class, savedDrop.getId());
        assertThat(remainingQuantity).isEqualTo(9);
    }

    @Test
    void 종료_시각과_같으면_주문이_거부된다() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        ProductDrop savedDrop = saveDrop(savedProduct, 10, 3, now.minusMinutes(20), now);

        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop.getId());

        MockHttpSession session = login(savedUser.getUsername());

        //when
        mockMvc.perform(post("/drops/{dropId}/orders", savedDrop.getId())
                        .session(session)
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "quantity" : 1
                                }
                                """))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DROP_NOT_ORDERABLE"));

        assertThat(purchaseOrderRepository.findAll()).isEmpty();
        Integer remainingQuantity = jdbcTemplate.queryForObject("SELECT remaining_quantity FROM product_drop WHERE id = ?", Integer.class, savedDrop.getId());
        assertThat(remainingQuantity).isEqualTo(10);
    }

    @Test
    void 승인되지_않은_드롭은_찾을_수_없다() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        ProductDrop savedDrop = saveDrop(savedProduct, 10, 3, now.minusMinutes(20), now.plusMinutes(10));

        MockHttpSession session = login(savedUser.getUsername());

        //when
        mockMvc.perform(post("/drops/{dropId}/orders", savedDrop.getId())
                        .session(session)
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "quantity" : 1
                                }
                                """))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DROP_NOT_FOUND"));

        assertThat(purchaseOrderRepository.findAll()).isEmpty();
        Integer remainingQuantity = jdbcTemplate.queryForObject("SELECT remaining_quantity FROM product_drop WHERE id = ?", Integer.class, savedDrop.getId());
        assertThat(remainingQuantity).isEqualTo(10);
    }

    @Test
    void 존재하지_않는_드롭은_찾을_수_없다() throws Exception {
        //given
        AppUser savedUser = saveUser("testuser123");

        MockHttpSession session = login(savedUser.getUsername());

        //when
        mockMvc.perform(post("/drops/{dropId}/orders",999L)
                        .session(session)
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "quantity" : 1
                                }
                                """))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DROP_NOT_FOUND"));

        assertThat(purchaseOrderRepository.findAll()).isEmpty();
    }

    @Test
    void 수량이_0이면_거부된다() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        ProductDrop savedDrop = saveDrop(savedProduct, 10, 3, now, now.plusMinutes(10));

        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop.getId());

        MockHttpSession session = login(savedUser.getUsername());

        //when
        mockMvc.perform(post("/drops/{dropId}/orders", savedDrop.getId())
                        .session(session)
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "quantity" : 0
                                }
                                """))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field").value("quantity"))
                .andExpect(jsonPath("$.errors[0].reason").value("OUT_OF_RANGE"));

        assertThat(purchaseOrderRepository.findAll()).isEmpty();
        Integer remainingQuantity = jdbcTemplate.queryForObject("SELECT remaining_quantity FROM product_drop WHERE id = ?", Integer.class, savedDrop.getId());
        assertThat(remainingQuantity).isEqualTo(10);
    }

    @Test
    void 로그인하지_않으면_거부된다() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        ProductDrop savedDrop = saveDrop(savedProduct, 10, 3, now, now.plusMinutes(10));

        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop.getId());

        //when
        mockMvc.perform(post("/drops/{dropId}/orders", savedDrop.getId())
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "quantity" : 2
                                }
                                """))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("LOGIN_REQUIRED"));

        assertThat(purchaseOrderRepository.findAll()).isEmpty();
        Integer remainingQuantity = jdbcTemplate.queryForObject("SELECT remaining_quantity FROM product_drop WHERE id = ?", Integer.class, savedDrop.getId());
        assertThat(remainingQuantity).isEqualTo(10);
    }

    private @NonNull PurchaseOrder setLastOrder(AppUser savedUser, ProductDrop savedDrop, int quantity,LocalDateTime now) {
        return purchaseOrderRepository.save(new PurchaseOrder(savedUser.getId(), savedDrop, quantity, now));
    }

    private @NonNull ProductDrop saveDrop(Product savedProduct, int limitedQuantity, int maxQuantityPerUser, LocalDateTime startAt, LocalDateTime endAt) {
        return productDropRepository.save(new ProductDrop(savedProduct, limitedQuantity, maxQuantityPerUser, startAt, endAt));
    }

    private @NonNull Product saveProduct(Store savedStore, int n) {
        return productRepository.save(new Product(savedStore, "testProduct" + n, "testDescription" + n, n * 1000, ProductCategory.FOOD));
    }

    private @NonNull AppUser saveUser(String username) {
        return appUserRepository.save(new AppUser(username, passwordEncoder.encode(username)));
    }

    private @NonNull Store saveStore(AppUser savedUser,String storeName) {
        return storeRepository.save(new Store(savedUser.getId(), storeName));
    }

    private MockHttpSession login(String username) throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/login")
                        .header("Origin","http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "username" : "%s",
                                "password" : "%s"
                                }
                                """
                                .formatted(username,username)))
                .andExpect(status().isNoContent())
                .andReturn().getRequest().getSession(false);
    }

}
