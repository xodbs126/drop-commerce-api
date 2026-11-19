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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;

@TestPropertySource(properties = {
        "logging.level.org.hibernate.SQL=debug"
})
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
    @Autowired
    private Clock clock;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbcTemplate;

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
