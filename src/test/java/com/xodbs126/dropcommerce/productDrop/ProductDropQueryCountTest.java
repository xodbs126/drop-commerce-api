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
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class ProductDropQueryCountTest extends IntegrationTest {

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
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManagerFactory emf;

    @TestBean
    private Clock clock;

    static Clock clock() {
        return Clock.fixed(Instant.parse("2026-11-19T02:00:00Z"), ZoneId.of("Asia/Seoul"));
    }

    @Test
    public void getProductDropListQueryCountWithOneCard() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        Statistics statistics = emf.unwrap(SessionFactory.class).getStatistics();
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        ProductDrop savedDrop = saveDrop(savedProduct, now);

        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop.getId());
        statistics.clear();

        //when
        mockMvc.perform(get("/drops"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1));

        System.out.println("count = " + statistics.getPrepareStatementCount());
    }

    @Test
    public void getProductDropListQueryCountWithThreeCardsOneStore() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        Statistics statistics = emf.unwrap(SessionFactory.class).getStatistics();
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");

        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);
        Product savedProduct3 = saveProduct(savedStore, 3);
        ProductDrop savedDrop1 = saveDrop(savedProduct1, now);
        ProductDrop savedDrop2 = saveDrop(savedProduct2, now);
        ProductDrop savedDrop3 = saveDrop(savedProduct3, now);

        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop3.getId());
        statistics.clear();

        //when
        mockMvc.perform(get("/drops"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(3));

        System.out.println("count = " + statistics.getPrepareStatementCount());
    }

    @Test
    public void getProductDropListQueryCountWithThreeCardsThreeStores() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now(clock);
        Statistics statistics = emf.unwrap(SessionFactory.class).getStatistics();
        AppUser savedUser1 = saveUser("testuser1");
        AppUser savedUser2 = saveUser("testuser2");
        AppUser savedUser3 = saveUser("testuser3");

        Store savedStore1 = saveStore(savedUser1, "testStore1");
        Store savedStore2 = saveStore(savedUser2, "testStore2");
        Store savedStore3 = saveStore(savedUser3, "testStore3");

        Product savedProduct1 = saveProduct(savedStore1, 1);
        Product savedProduct2 = saveProduct(savedStore2, 2);
        Product savedProduct3 = saveProduct(savedStore3, 3);
        ProductDrop savedDrop1 = saveDrop(savedProduct1, now);
        ProductDrop savedDrop2 = saveDrop(savedProduct2, now);
        ProductDrop savedDrop3 = saveDrop(savedProduct3, now);

        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop1.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop2.getId());
        jdbcTemplate.update("UPDATE product_drop SET approved_at = ?, status = ? WHERE id = ?", now, "APPROVED", savedDrop3.getId());
        statistics.clear();

        //when
        mockMvc.perform(get("/drops"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(3));

        System.out.println("count = " + statistics.getPrepareStatementCount());
    }

    private @NonNull Product saveProduct(Store savedStore, int num) {
        return productRepository.save(new Product(savedStore, "testProduct" + num, "testDescription" + num, num * 1000, ProductCategory.BOOK));
    }

    private @NonNull Store saveStore(AppUser savedUser, String storeName) {
        return storeRepository.save(new Store(savedUser.getId(), storeName));
    }

    private @NonNull AppUser saveUser(String username) {
        return appUserRepository.save(new AppUser(username, passwordEncoder.encode(username)));
    }

    private @NonNull ProductDrop saveDrop(Product savedProduct, LocalDateTime now) {
        return saveDrop(savedProduct, now.minusMinutes(10), now.plusMinutes(10));
    }

    private @NonNull ProductDrop saveDrop(Product savedProduct, LocalDateTime startAt, LocalDateTime endAt) {
        return productDropRepository.save(new ProductDrop(savedProduct, 10, 2, startAt, endAt));
    }
}
