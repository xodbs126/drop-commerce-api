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
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        Store savedStore = storeRepository.save(new Store(savedUser.getId(), "testStore1"));
        Product savedProduct = savedProduct(savedStore, 1);
        Product savedProduct2 = savedProduct(savedStore, 2);
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

    private @NonNull ProductDrop saveDrop(Product savedProduct, LocalDateTime now) {
        return productDropRepository.save(new ProductDrop(savedProduct, 10, 2, now.minusMinutes(10), now.plusMinutes(10)));
    }

    private @NonNull Product savedProduct(Store savedStore,int n) {
        return productRepository.save(new Product(savedStore, "testProduct" + n, "testDescription" + n, n * 1000, ProductCategory.FOOD));
    }

    private @NonNull AppUser saveUser(String username) {
        return appUserRepository.save(new AppUser(username, passwordEncoder.encode(username)));
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
