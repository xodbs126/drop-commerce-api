package com.xodbs126.dropcommerce.productDrop;

import com.xodbs126.dropcommerce.IntegrationTest;
import com.xodbs126.dropcommerce.appuser.entity.AppUser;
import com.xodbs126.dropcommerce.appuser.repository.AppUserRepository;
import com.xodbs126.dropcommerce.product.entity.Product;
import com.xodbs126.dropcommerce.product.entity.ProductCategory;
import com.xodbs126.dropcommerce.product.repository.ProductRepository;
import com.xodbs126.dropcommerce.productdrop.entity.ProductDrop;
import com.xodbs126.dropcommerce.productdrop.repository.ProductDropRepository;
import com.xodbs126.dropcommerce.store.entity.Store;
import com.xodbs126.dropcommerce.store.repository.StoreRepository;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class ProductDropListTest extends IntegrationTest {

    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductDropRepository productDropRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @TestBean
    private Clock clock;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    static Clock clock() {
        return Clock.fixed(Instant.parse("2026-11-19T02:00:00Z"), ZoneId.of("Asia/Seoul"));
    }

    @Test
    public void getProductDropListWithoutDeletedProduct() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);
        ProductDrop savedDrop = saveDrop(savedProduct, now);
        ProductDrop savedDrop2 = saveDrop(savedProduct2, now);
        jdbcTemplate.update("UPDATE product SET deleted_at = ? WHERE id = ?", now, savedProduct2.getId());

        jdbcTemplate.update("UPDATE product_drop SET approved_at=?, status = ? WHERE id = ?", now, "APPROVED", savedDrop.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at=?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());

        //when
        mockMvc.perform(get("/drops"))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].productName").value("testProduct1"))
                .andExpect(jsonPath("$.content[0].storeName").value("testStore1"))
                .andExpect(jsonPath("$.content[0].description").doesNotExist())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    public void getProductDropSuccess() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");

        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);
        Product savedProduct3 = saveProduct(savedStore, 3);

        ProductDrop savedDrop1 = saveDrop(savedProduct1, now.plusMinutes(10), now.plusMinutes(20));
        ProductDrop savedDrop2 = saveDrop(savedProduct2, now.plusMinutes(-10), now.plusMinutes(10));
        ProductDrop savedDrop3 = saveDrop(savedProduct3, now.plusMinutes(-20), now.plusMinutes(-10));

        jdbcTemplate.update("UPDATE product_drop SET approved_at=?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at=?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at=?, status = ? WHERE id = ?", now, "APPROVED", savedDrop3.getId());

        mockMvc.perform(get("/drops"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[*].productName",
                        containsInAnyOrder("testProduct1", "testProduct2")));
    }

    @Test
    public void getProductDropWithUpcoming() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");

        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);
        Product savedProduct3 = saveProduct(savedStore, 3);

        ProductDrop savedDrop1 = saveDrop(savedProduct1, now.plusMinutes(10), now.plusMinutes(20));
        ProductDrop savedDrop2 = saveDrop(savedProduct2, now.plusMinutes(-10), now.plusMinutes(10));
        ProductDrop savedDrop3 = saveDrop(savedProduct3, now.plusMinutes(-20), now.plusMinutes(-10));

        jdbcTemplate.update("UPDATE product_drop SET approved_at=?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at=?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at=?, status = ? WHERE id = ?", now, "APPROVED", savedDrop3.getId());

        mockMvc.perform(get("/drops")
                        .param("phase", "UPCOMING"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].productName").value("testProduct1"))
                .andExpect(jsonPath("$.content[0].phase").value("UPCOMING"));
    }

    @Test
    public void getProductDropWithOngoing() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");

        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);
        Product savedProduct3 = saveProduct(savedStore, 3);

        ProductDrop savedDrop1 = saveDrop(savedProduct1, now.plusMinutes(10), now.plusMinutes(20));
        ProductDrop savedDrop2 = saveDrop(savedProduct2, now.plusMinutes(-10), now.plusMinutes(10));
        ProductDrop savedDrop3 = saveDrop(savedProduct3, now.plusMinutes(-20), now.plusMinutes(-10));

        jdbcTemplate.update("UPDATE product_drop SET approved_at=?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at=?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at=?, status = ? WHERE id = ?", now, "APPROVED", savedDrop3.getId());

        mockMvc.perform(get("/drops")
                        .param("phase", "ONGOING"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].productName").value("testProduct2"))
                .andExpect(jsonPath("$.content[0].phase").value("ONGOING"));
    }

    @Test
    public void getProductDropWithEnded() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");

        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);
        Product savedProduct3 = saveProduct(savedStore, 3);

        ProductDrop savedDrop1 = saveDrop(savedProduct1, now.plusMinutes(10), now.plusMinutes(20));
        ProductDrop savedDrop2 = saveDrop(savedProduct2, now.plusMinutes(-10), now.plusMinutes(10));
        ProductDrop savedDrop3 = saveDrop(savedProduct3, now.plusMinutes(-20), now.plusMinutes(-10));

        jdbcTemplate.update("UPDATE product_drop SET approved_at=?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at=?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at=?, status = ? WHERE id = ?", now, "APPROVED", savedDrop3.getId());

        mockMvc.perform(get("/drops")
                        .param("phase", "ENDED"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].productName").value("testProduct3"))
                .andExpect(jsonPath("$.content[0].phase").value("ENDED"));
    }

    @Test
    public void getProductDropListWithoutCancelled() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");

        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);

        ProductDrop savedDrop1 = saveDrop(savedProduct1, now.minusMinutes(10), now.plusMinutes(20));
        ProductDrop savedDrop2 = saveDrop(savedProduct2, now.minusMinutes(10), now.plusMinutes(20));

        jdbcTemplate.update("UPDATE product_drop SET approved_at=?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at=?, status = ? WHERE id = ?", now, "CANCELLED", savedDrop2.getId());


        mockMvc.perform(get("/drops")
                        .param("phase", "ONGOING"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].productName").value(savedDrop1.getProduct().getName()));
    }

    @Test
    public void getProductDropListWithCancelledAfterApproval() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");

        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);

        ProductDrop approvedDrop = saveDrop(savedProduct1, now.minusMinutes(10), now.plusMinutes(20));
        ProductDrop cancelledDrop = saveDrop(savedProduct2, now.minusMinutes(10), now.plusMinutes(20));

        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", approvedDrop.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "CANCELLED", cancelledDrop.getId());

        //when
        mockMvc.perform(get("/drops"))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[*].productName",
                        containsInAnyOrder("testProduct1", "testProduct2")));
    }

    @Test
    public void getProductDropWithEndedIncludingCancelled() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");

        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);

        ProductDrop approvedDrop = saveDrop(savedProduct1, now.minusMinutes(20), now.minusMinutes(10));
        ProductDrop cancelledDrop = saveDrop(savedProduct2, now.minusMinutes(20), now.minusMinutes(10));

        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", approvedDrop.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "CANCELLED", cancelledDrop.getId());

        //when
        mockMvc.perform(get("/drops")
                        .param("phase", "ENDED"))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[*].productName",
                        containsInAnyOrder("testProduct1", "testProduct2")));
    }

    @Test
    public void getProductDropWithUpcomingWithoutCancelled() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");

        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);

        ProductDrop approvedDrop = saveDrop(savedProduct1, now.plusMinutes(10), now.plusMinutes(20));
        ProductDrop cancelledDrop = saveDrop(savedProduct2, now.plusMinutes(10), now.plusMinutes(20));

        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", approvedDrop.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "CANCELLED", cancelledDrop.getId());

        //when
        mockMvc.perform(get("/drops")
                        .param("phase", "UPCOMING"))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].productName").value("testProduct1"));
    }

    @Test
    public void getProductDropListWithoutUnapproved() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");

        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);
        Product savedProduct3 = saveProduct(savedStore, 3);
        Product savedProduct4 = saveProduct(savedStore, 4);

        ProductDrop approvedDrop = saveDrop(savedProduct1, now.minusMinutes(10), now.plusMinutes(20));
        saveDrop(savedProduct2, now.minusMinutes(10), now.plusMinutes(20));
        ProductDrop rejectedDrop = saveDrop(savedProduct3, now.minusMinutes(10), now.plusMinutes(20));
        ProductDrop cancelledBeforeApprovalDrop = saveDrop(savedProduct4, now.minusMinutes(10), now.plusMinutes(20));

        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", approvedDrop.getId());
        jdbcTemplate.update("UPDATE product_drop SET status = ? WHERE id = ?", "REJECTED", rejectedDrop.getId());
        jdbcTemplate.update("UPDATE product_drop SET status = ? WHERE id = ?", "CANCELLED", cancelledBeforeApprovalDrop.getId());

        //when
        mockMvc.perform(get("/drops"))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].productName").value("testProduct1"));
    }

    @Test
    public void getProductDropWithOngoingAtStartBoundary() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);

        ProductDrop drop = saveDrop(savedProduct, now, now.plusMinutes(10));
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", drop.getId());

        //when
        mockMvc.perform(get("/drops")
                        .param("phase", "ONGOING"))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].productName").value("testProduct1"))
                .andExpect(jsonPath("$.content[0].phase").value("ONGOING"));
    }

    @Test
    public void getProductDropWithEndedAtEndBoundary() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);

        ProductDrop drop = saveDrop(savedProduct, now.minusMinutes(10), now);
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", drop.getId());

        //when
        mockMvc.perform(get("/drops")
                        .param("phase", "ENDED"))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].productName").value("testProduct1"))
                .andExpect(jsonPath("$.content[0].phase").value("ENDED"));
    }

    @Test
    public void getProductDropWithCategory() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);

        ProductDrop savedDrop1 = saveDrop(savedProduct1, now.minusMinutes(10), now.plusMinutes(10));
        ProductDrop savedDrop2 = saveDrop(savedProduct2, now.minusMinutes(10), now.plusMinutes(10));
        jdbcTemplate.update("UPDATE product SET category = ? WHERE id = ?", "BOOK", savedProduct2.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());

        //when
        mockMvc.perform(get("/drops")
                        .param("category", "FOOD"))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].category").value("FOOD"));
    }

    @Test
    public void getProductDropWithoutCategory() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);

        ProductDrop savedDrop1 = saveDrop(savedProduct1, now.minusMinutes(10), now.plusMinutes(10));
        ProductDrop savedDrop2 = saveDrop(savedProduct2, now.minusMinutes(10), now.plusMinutes(10));
        jdbcTemplate.update("UPDATE product SET category = ? WHERE id = ?", "BOOK", savedProduct2.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());

        //when
        mockMvc.perform(get("/drops"))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].category", containsInAnyOrder("FOOD", "BOOK")));
    }

    @Test
    public void getProductDropWithName() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);

        ProductDrop savedDrop1 = saveDrop(savedProduct1, now.minusMinutes(10), now.plusMinutes(10));
        ProductDrop savedDrop2 = saveDrop(savedProduct2, now.minusMinutes(10), now.plusMinutes(10));
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());

        //when
        mockMvc.perform(get("/drops")
                        .param("keyword", "testProduct1"))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].productName").value(savedDrop1.getProduct().getName()));
    }

    @Test
    public void getProductDropWithDescription() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);

        ProductDrop savedDrop1 = saveDrop(savedProduct1, now.minusMinutes(10), now.plusMinutes(10));
        ProductDrop savedDrop2 = saveDrop(savedProduct2, now.minusMinutes(10), now.plusMinutes(10));
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());

        //when
        mockMvc.perform(get("/drops")
                        .param("keyword", "testDescription1"))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].productName").value(savedDrop1.getProduct().getName()));
    }

    @Test
    public void getProductDropWithKeywordAndOngoing() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);
        Product savedProduct3 = saveProduct(savedStore, 3);

        jdbcTemplate.update("UPDATE product SET description = ? WHERE id = ?", "testDescription1", savedProduct2.getId());

        ProductDrop savedDrop1 = saveDrop(savedProduct1, now.minusMinutes(10), now.plusMinutes(10));
        ProductDrop savedDrop2 = saveDrop(savedProduct2, now.plusMinutes(10), now.plusMinutes(20));
        ProductDrop savedDrop3 = saveDrop(savedProduct3, now.minusMinutes(10), now.plusMinutes(10));
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop3.getId());

        //when
        mockMvc.perform(get("/drops")
                        .param("keyword", "testDescription1")
                        .param("phase", "ONGOING"))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].productName").value(savedDrop1.getProduct().getName()));
    }

    @Test
    public void getProductDropWithStoreName() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        AppUser otherUser = saveUser("otheruser123");
        Store savedStore1 = saveStore(savedUser, "testStore1");
        Store savedStore2 = saveStore(otherUser, "testStore2");
        Product savedProduct1 = saveProduct(savedStore1, 1);
        Product savedProduct2 = saveProduct(savedStore2, 2);

        ProductDrop savedDrop1 = saveDrop(savedProduct1, now.minusMinutes(10), now.plusMinutes(10));
        ProductDrop savedDrop2 = saveDrop(savedProduct2, now.minusMinutes(10), now.plusMinutes(10));
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());

        //when
        mockMvc.perform(get("/drops")
                        .param("keyword", "testStore1"))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].productName").value(savedDrop1.getProduct().getName()));
    }

    @Test
    public void getProductDropListOrderByStartAt() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);
        Product savedProduct3 = saveProduct(savedStore, 3);

        ProductDrop savedDrop1 = saveDrop(savedProduct1, now.plusMinutes(20), now.plusMinutes(30));
        ProductDrop savedDrop2 = saveDrop(savedProduct2, now, now.plusMinutes(30));
        ProductDrop savedDrop3 = saveDrop(savedProduct3, now.plusMinutes(10), now.plusMinutes(30));
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop3.getId());

        //when
        mockMvc.perform(get("/drops"))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(3))
                .andExpect(jsonPath("$.content[*].productName",
                        contains("testProduct2", "testProduct3", "testProduct1")));
    }

    @Test
    public void getProductDropListOrderByIdWhenSameStartAt() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);
        Product savedProduct3 = saveProduct(savedStore, 3);

        ProductDrop savedDrop1 = saveDrop(savedProduct3, now, now.plusMinutes(30));
        ProductDrop savedDrop2 = saveDrop(savedProduct1, now, now.plusMinutes(30));
        ProductDrop savedDrop3 = saveDrop(savedProduct2, now, now.plusMinutes(30));
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop3.getId());

        //when
        mockMvc.perform(get("/drops"))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(3))
                .andExpect(jsonPath("$.content[*].productName",
                        contains("testProduct3", "testProduct1", "testProduct2")));
    }

    @Test
    public void getProductDropWithHasNext() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);
        Product savedProduct3 = saveProduct(savedStore, 3);
        Product savedProduct4 = saveProduct(savedStore, 4);

        ProductDrop savedDrop1 = saveDrop(savedProduct3, now, now.plusMinutes(60));
        ProductDrop savedDrop2 = saveDrop(savedProduct1, now.plusMinutes(10), now.plusMinutes(60));
        ProductDrop savedDrop3 = saveDrop(savedProduct2, now.plusMinutes(20), now.plusMinutes(60));
        ProductDrop savedDrop4 = saveDrop(savedProduct4, now.plusMinutes(30), now.plusMinutes(60));

        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop3.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop4.getId());

        mockMvc.perform(get("/drops")
                        .param("page", "0")
                        .param("size", "2"))
                .andDo(print())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[0].productName").value("testProduct3"))
                .andExpect(jsonPath("$.content[1].productName").value("testProduct1"))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2));
    }

    @Test
    public void getProductDropWithFit() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);
        Product savedProduct3 = saveProduct(savedStore, 3);
        Product savedProduct4 = saveProduct(savedStore, 4);

        ProductDrop savedDrop1 = saveDrop(savedProduct3, now, now.plusMinutes(60));
        ProductDrop savedDrop2 = saveDrop(savedProduct1, now.plusMinutes(10), now.plusMinutes(60));
        ProductDrop savedDrop3 = saveDrop(savedProduct2, now.plusMinutes(20), now.plusMinutes(60));
        ProductDrop savedDrop4 = saveDrop(savedProduct4, now.plusMinutes(30), now.plusMinutes(60));

        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop3.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop4.getId());

        mockMvc.perform(get("/drops")
                        .param("page", "1")
                        .param("size", "2"))
                .andDo(print())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[0].productName").value("testProduct2"))
                .andExpect(jsonPath("$.content[1].productName").value("testProduct4"))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2));
    }

    @Test
    public void getProductDropWithLastOne() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);
        Product savedProduct3 = saveProduct(savedStore, 3);

        ProductDrop savedDrop1 = saveDrop(savedProduct3, now, now.plusMinutes(60));
        ProductDrop savedDrop2 = saveDrop(savedProduct1, now.plusMinutes(10), now.plusMinutes(60));
        ProductDrop savedDrop3 = saveDrop(savedProduct2, now.plusMinutes(20), now.plusMinutes(60));
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop3.getId());

        mockMvc.perform(get("/drops")
                        .param("page", "1")
                        .param("size", "2"))
                .andDo(print())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].productName").value("testProduct2"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    public void getProductDropListWithEndedOrderByStartAtDesc() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);
        Product savedProduct3 = saveProduct(savedStore, 3);

        ProductDrop savedDrop1 = saveDrop(savedProduct1, now.minusMinutes(30), now.minusMinutes(5));
        ProductDrop savedDrop2 = saveDrop(savedProduct2, now.minusMinutes(60), now.minusMinutes(5));
        ProductDrop savedDrop3 = saveDrop(savedProduct3, now.minusMinutes(10), now.minusMinutes(5));
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop3.getId());

        //when
        mockMvc.perform(get("/drops")
                        .param("phase", "ENDED"))
                .andDo(print())
                //then
                .andExpect(jsonPath("$.content.size()").value(3))
                .andExpect(jsonPath("$.content[*].productName",
                        contains("testProduct3", "testProduct1", "testProduct2")));
    }

    @Test
    public void getProductDropListWithEndedOrderByIdWhenSameStartAt() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);
        Product savedProduct3 = saveProduct(savedStore, 3);

        ProductDrop savedDrop1 = saveDrop(savedProduct1, now.minusMinutes(60), now.minusMinutes(5));
        ProductDrop savedDrop2 = saveDrop(savedProduct2, now.minusMinutes(60), now.minusMinutes(5));
        ProductDrop savedDrop3 = saveDrop(savedProduct3, now.minusMinutes(60), now.minusMinutes(5));
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop3.getId());

        //when
        mockMvc.perform(get("/drops")
                        .param("phase", "ENDED"))
                .andDo(print())
                //then
                .andExpect(jsonPath("$.content.size()").value(3))
                .andExpect(jsonPath("$.content[*].productName",
                        contains("testProduct1", "testProduct2", "testProduct3")));
    }

    @Test
    public void getProductDropListOrderByPriceAsc() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct1 = saveProduct(savedStore, 2);
        Product savedProduct2 = saveProduct(savedStore, 1);
        Product savedProduct3 = saveProduct(savedStore, 3);

        ProductDrop savedDrop1 = saveDrop(savedProduct1, now.minusMinutes(60), now.plusMinutes(5));
        ProductDrop savedDrop2 = saveDrop(savedProduct2, now.minusMinutes(60), now.plusMinutes(5));
        ProductDrop savedDrop3 = saveDrop(savedProduct3, now.minusMinutes(60), now.plusMinutes(5));
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop3.getId());

        //when
        mockMvc.perform(get("/drops")
                        .param("sort", "price,asc"))
                .andDo(print())
                //then
                .andExpect(jsonPath("$.content.size()").value(3))
                .andExpect(jsonPath("$.content[0].productName").value("testProduct1"))
                .andExpect(jsonPath("$.content[1].productName").value("testProduct2"))
                .andExpect(jsonPath("$.content[2].productName").value("testProduct3"));
    }

    @Test
    public void getProductDropListOrderByPriceDesc() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct1 = saveProduct(savedStore, 2);
        Product savedProduct2 = saveProduct(savedStore, 1);
        Product savedProduct3 = saveProduct(savedStore, 3);

        ProductDrop savedDrop1 = saveDrop(savedProduct1, now.minusMinutes(60), now.plusMinutes(5));
        ProductDrop savedDrop2 = saveDrop(savedProduct2, now.minusMinutes(60), now.plusMinutes(5));
        ProductDrop savedDrop3 = saveDrop(savedProduct3, now.minusMinutes(60), now.plusMinutes(5));
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop3.getId());

        //when
        mockMvc.perform(get("/drops")
                        .param("sort", "price,desc"))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(3))
                .andExpect(jsonPath("$.content[0].productName").value("testProduct3"))
                .andExpect(jsonPath("$.content[1].productName").value("testProduct2"))
                .andExpect(jsonPath("$.content[2].productName").value("testProduct1"));
    }

    @Test
    public void getProductDropListOrderByIdWhenPriceSame() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);
        Product savedProduct3 = saveProduct(savedStore, 3);
        jdbcTemplate.update("UPDATE product SET price = 1000");

        ProductDrop savedDrop1 = saveDrop(savedProduct1, now.minusMinutes(60), now.plusMinutes(5));
        ProductDrop savedDrop2 = saveDrop(savedProduct2, now.minusMinutes(60), now.plusMinutes(5));
        ProductDrop savedDrop3 = saveDrop(savedProduct3, now.minusMinutes(60), now.plusMinutes(5));
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop3.getId());

        //when
        mockMvc.perform(get("/drops")
                        .param("sort", "price,desc"))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(3))
                .andExpect(jsonPath("$.content[0].productName").value("testProduct1"))
                .andExpect(jsonPath("$.content[1].productName").value("testProduct2"))
                .andExpect(jsonPath("$.content[2].productName").value("testProduct3"));
    }

    @Test
    public void getProductDropListOrderByLimitedQuantity() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);
        Product savedProduct3 = saveProduct(savedStore, 3);

        ProductDrop savedDrop1 = saveDrop(savedProduct1, now.minusMinutes(60), now.plusMinutes(5));
        ProductDrop savedDrop2 = saveDrop(savedProduct2, now.minusMinutes(60), now.plusMinutes(5));
        ProductDrop savedDrop3 = saveDrop(savedProduct3, now.minusMinutes(60), now.plusMinutes(5));
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, limited_quantity = ?, status = ? WHERE id = ?", now, 3, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, limited_quantity = ?, status = ? WHERE id = ?", now, 9, "APPROVED", savedDrop2.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, limited_quantity = ?, status = ? WHERE id = ?", now, 6, "APPROVED", savedDrop3.getId());

        //when
        mockMvc.perform(get("/drops")
                        .param("sort", "limitedQuantity,desc"))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(3))
                .andExpect(jsonPath("$.content[0].productName").value("testProduct2"))
                .andExpect(jsonPath("$.content[1].productName").value("testProduct3"))
                .andExpect(jsonPath("$.content[2].productName").value("testProduct1"));
    }

    @Test
    public void getProductDropListOrderByRemainingQuantity() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);
        Product savedProduct3 = saveProduct(savedStore, 3);

        ProductDrop savedDrop1 = saveDrop(savedProduct1, now.minusMinutes(60), now.plusMinutes(5));
        ProductDrop savedDrop2 = saveDrop(savedProduct2, now.minusMinutes(60), now.plusMinutes(5));
        ProductDrop savedDrop3 = saveDrop(savedProduct3, now.minusMinutes(60), now.plusMinutes(5));
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, remaining_quantity = ?, status = ? WHERE id = ?", now, 1, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, remaining_quantity = ?, status = ? WHERE id = ?", now, 5, "APPROVED", savedDrop2.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, remaining_quantity = ?, status = ? WHERE id = ?", now, 3, "APPROVED", savedDrop3.getId());

        //when
        mockMvc.perform(get("/drops")
                        .param("sort", "remainingQuantity,desc"))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(3))
                .andExpect(jsonPath("$.content[0].productName").value("testProduct2"))
                .andExpect(jsonPath("$.content[1].productName").value("testProduct3"))
                .andExpect(jsonPath("$.content[2].productName").value("testProduct1"));
    }

    @Test
    public void getProductDropListWithEndedOrderByStartAtAsc() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);
        Product savedProduct3 = saveProduct(savedStore, 3);

        ProductDrop savedDrop1 = saveDrop(savedProduct1, now.minusMinutes(60), now.minusMinutes(5));
        ProductDrop savedDrop2 = saveDrop(savedProduct2, now.minusMinutes(50), now.minusMinutes(5));
        ProductDrop savedDrop3 = saveDrop(savedProduct3, now.minusMinutes(40), now.minusMinutes(5));
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop3.getId());

        //when
        mockMvc.perform(get("/drops")
                        .param("phase", "ENDED")
                        .param("sort", "startAt,asc"))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(3))
                .andExpect(jsonPath("$.content[0].productName").value("testProduct1"))
                .andExpect(jsonPath("$.content[0].phase").value("ENDED"))
                .andExpect(jsonPath("$.content[1].productName").value("testProduct2"))
                .andExpect(jsonPath("$.content[1].phase").value("ENDED"))
                .andExpect(jsonPath("$.content[2].productName").value("testProduct3"))
                .andExpect(jsonPath("$.content[2].phase").value("ENDED"));
    }

    @Test
    public void getProductDropWithNoSortArgument() throws Exception {
        //when
        mockMvc.perform(get("/drops")
                        .param("sort", "price"))
                .andDo(print())
                //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[?(@.field == 'sort')].reason", hasItem("INVALID_FORMAT")));
    }

    @Test
    public void getProductDropWithNotAllowedSort() throws Exception {
        //when
        mockMvc.perform(get("/drops")
                        .param("sort", "name,asc"))
                .andDo(print())
                //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[?(@.field == 'sort')].reason", hasItem("INVALID_FORMAT")));
    }

    @Test
    public void getProductDropWithSortUpper() throws Exception {
        //when
        mockMvc.perform(get("/drops")
                        .param("sort", "price,ASC"))
                .andDo(print())
                //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[?(@.field == 'sort')].reason", hasItem("INVALID_FORMAT")));
    }

    @Test
    public void getProductDropWithSortBlank() throws Exception {
        //when
        mockMvc.perform(get("/drops")
                        .param("sort", ""))
                .andDo(print())
                //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[?(@.field == 'sort')].reason", hasItem("INVALID_FORMAT")));
    }

    private @NonNull ProductDrop saveDrop(Product savedProduct, LocalDateTime now) {
        return saveDrop(savedProduct, now.minusMinutes(10), now.plusMinutes(10));
    }

    private @NonNull ProductDrop saveDrop(Product savedProduct, LocalDateTime startAt, LocalDateTime endAt) {
        return productDropRepository.save(new ProductDrop(savedProduct, 10, 2, startAt, endAt));
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

}
