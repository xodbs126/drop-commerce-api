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
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.nullValue;

@AutoConfigureMockMvc
public class ProductDropDetailTest extends IntegrationTest {

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
    @Autowired
    private Clock clock;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void getProductDropSuccess() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser,"testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);
        ProductDrop savedDrop = saveDrop(savedProduct2, now);
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop.getId());
        //when
        mockMvc.perform(get("/drops/{dropId}", savedDrop.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dropId").value(savedDrop.getId()))
                .andExpect(jsonPath("$.productName").value("testProduct2"))
                .andExpect(jsonPath("$.description").value("testDescription2"))
                .andExpect(jsonPath("$.category").value("FOOD"))
                .andExpect(jsonPath("$.price").value(2000))
                .andExpect(jsonPath("$.storeName").value("testStore1"))
                .andExpect(jsonPath("$.phase").value("ONGOING"))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.remainingQuantity").value(10L))
                .andExpect(jsonPath("$.stock").doesNotExist());
    }

    @Test
    public void getProductDropWithNoId() throws  Exception{
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        ProductDrop savedDrop = saveDrop(savedProduct, now);

        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop.getId());

        //when
        mockMvc.perform(get("/drops/{dropId}", 999L))
                //then
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DROP_NOT_FOUND"));
    }

    @Test
    public void getProductDropWithPending() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        ProductDrop savedDrop = saveDrop(savedProduct, now);

        //when
        mockMvc.perform(get("/drops/{dropId}", savedDrop.getId()))
                //then
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DROP_NOT_FOUND"));
    }

    @Test
    public void getProductDropWithRejected() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        ProductDrop savedDrop = saveDrop(savedProduct, now);

        jdbcTemplate.update("UPDATE product_drop SET status = ? WHERE id = ?", "REJECTED", savedDrop.getId());

        //when
        mockMvc.perform(get("/drops/{dropId}", savedDrop.getId()))
                //then
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DROP_NOT_FOUND"));
    }

    @Test
    public void getProductDropWithCancelledBeforeApproved() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        ProductDrop savedDrop = saveDrop(savedProduct, now);

        jdbcTemplate.update("UPDATE product_drop SET status = ? WHERE id = ?", "CANCELLED", savedDrop.getId());

        //when
        mockMvc.perform(get("/drops/{dropId}", savedDrop.getId()))
                //then
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DROP_NOT_FOUND"));
    }

    @Test
    public void getProductDropWithCancelledAfterApproved() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        ProductDrop savedDrop = saveDrop(savedProduct, now);

        jdbcTemplate.update("UPDATE product_drop SET approved_at=?, status = ? WHERE id = ?", now, "CANCELLED", savedDrop.getId());

        //when
        mockMvc.perform(get("/drops/{dropId}", savedDrop.getId()))
                //then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.phase").value(nullValue()));
    }

    @Test
    public void getProductDropWithUpcoming() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        ProductDrop savedDrop = saveDrop(savedProduct, now.plusMinutes(10), now.plusMinutes(20));

        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop.getId());

        //when
        mockMvc.perform(get("/drops/{dropId}", savedDrop.getId()))
                //then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phase").value("UPCOMING"))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    public void getProductDropWithEnded() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        ProductDrop savedDrop = saveDrop(savedProduct, now.minusMinutes(20), now.minusMinutes(10));

        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now.minusMinutes(30), "APPROVED", savedDrop.getId());

        //when
        mockMvc.perform(get("/drops/{dropId}", savedDrop.getId()))
                //then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phase").value("ENDED"))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    public void getProductDropWithDeletedProduct() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        ProductDrop savedDrop = saveDrop(savedProduct, now);

        jdbcTemplate.update("UPDATE product_drop SET approved_at=?, status = ? WHERE id = ?", now, "APPROVED", savedDrop.getId());
        jdbcTemplate.update("UPDATE product SET deleted_at = ? WHERE id = ?", now, savedProduct.getId());
        //when
        mockMvc.perform(get("/drops/{dropId}", savedDrop.getId()))
                //then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("testProduct1"))
                .andExpect(jsonPath("$.description").value("testDescription1"))
                .andExpect(jsonPath("$.price").value(1000))
                .andExpect(jsonPath("$.storeName").value("testStore1"))
                .andExpect(jsonPath("$.status").value("APPROVED"));
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
