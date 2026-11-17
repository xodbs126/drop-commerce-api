package com.xodbs126.dropcommerce.product;

import com.xodbs126.dropcommerce.IntegrationTest;
import com.xodbs126.dropcommerce.appuser.entity.AppUser;
import com.xodbs126.dropcommerce.appuser.repository.AppUserRepository;
import com.xodbs126.dropcommerce.product.entity.Product;
import com.xodbs126.dropcommerce.product.entity.ProductCategory;
import com.xodbs126.dropcommerce.product.repository.ProductRepository;
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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class ProductStockTest extends IntegrationTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void updateProductStockSuccess() throws Exception {
        //given
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser,"testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        jdbcTemplate.update("UPDATE product SET stock = ? WHERE id = ?", 10, savedProduct.getId());

        MockHttpSession session = login(savedUser.getUsername());

        //when
        mockMvc.perform(patch("/stores/me/products/{productId}/stock", savedProduct.getId())
                        .header("Origin", "http://localhost:3000")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "delta" : 10
                                }
                                """))
                .andDo(print())
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(savedProduct.getId()))
                .andExpect(jsonPath("$.stock").value(20));

        Integer targetStock = jdbcTemplate.queryForObject("SELECT stock FROM product WHERE id = ?", Integer.class, savedProduct.getId());
        assertThat(targetStock).isEqualTo(20);
    }

    @Test
    public void updateProductStockWithMinus() throws Exception {
        //given
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore123");
        Product savedProduct = saveProduct(savedStore, 1);
        jdbcTemplate.update("UPDATE product SET stock = ? WHERE id = ?", 6, savedProduct.getId());

        MockHttpSession session = login("testuser123");

        //when
        mockMvc.perform(patch("/stores/me/products/{productId}/stock", savedProduct.getId())
                        .session(session)
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "delta" : -7
                                }
                                """))
                .andDo(print())
                //then
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("STOCK_INSUFFICIENT"));

        Integer targetStock = jdbcTemplate.queryForObject("SELECT stock FROM product WHERE id = ?", Integer.class, savedProduct.getId());
        assertThat(targetStock).isEqualTo(6);
    }

    @Test
    public void updateProductStockToZero() throws Exception {
        //given
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);

        jdbcTemplate.update("UPDATE product SET stock = ? WHERE id = ?", 6, savedProduct.getId());

        MockHttpSession session = login("testuser123");

        //when
        mockMvc.perform(patch("/stores/me/products/{productId}/stock", savedProduct.getId())
                        .session(session)
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "delta": -6
                                }
                                """))
                .andDo(print())
                //then
                .andExpect(status().isOk());

        Integer result = jdbcTemplate.queryForObject("SELECT stock FROM product WHERE id = ?", Integer.class, savedProduct.getId());
        assertThat(result).isEqualTo(0);
    }

    @Test
    public void updateProductStockWithNone() throws Exception {
        //given
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);

        MockHttpSession session = login("testuser123");

        //when
        mockMvc.perform(patch("/stores/me/products/{productId}/stock", savedProduct.getId())
                        .session(session)
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {}
                                """))
                .andDo(print())
                //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[?(@.field=='delta')].reason", hasItem("REQUIRED")));
    }

    @Test
    public void updateProductStockWithDeltaZero() throws Exception {
        //given
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);

        MockHttpSession session = login("testuser123");

        //when
        mockMvc.perform(patch("/stores/me/products/{productId}/stock", savedProduct.getId())
                        .session(session)
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "delta" : 0
                                }
                                """))
                .andDo(print())
                //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field=='delta')].reason", hasItem("INVALID_VALUE")));
    }

    @Test
    public void updateProductStockWithAnotherStore() throws Exception {
        //given
        AppUser anotherUser = saveUser("anotheruser");
        saveStore(anotherUser, "testStore123");

        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        jdbcTemplate.update("UPDATE product SET stock = ? WHERE id = ?", 10, savedProduct.getId());
        MockHttpSession session = login("anotheruser");

        //when
        mockMvc.perform(patch("/stores/me/products/{productId}/stock", savedProduct.getId())
                        .session(session)
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "delta" : 10
                                }
                                """))
                .andDo(print())
                //then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));

        Integer result = jdbcTemplate.queryForObject("SELECT stock FROM product WHERE id = ?", Integer.class, savedProduct.getId());
        assertThat(result).isEqualTo(10);
    }

    @Test
    public void updateProductStockWithoutStore() throws Exception {
        //given
        AppUser anotherUser = saveUser("anotheruser");

        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        jdbcTemplate.update("UPDATE product SET stock = ? WHERE id = ?", 10, savedProduct.getId());
        MockHttpSession session = login("anotheruser");

        //when
        mockMvc.perform(patch("/stores/me/products/{productId}/stock", savedProduct.getId())
                        .session(session)
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "delta" : 10
                                }
                                """))
                .andDo(print())
                //then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("STORE_NOT_FOUND"));

        Integer result = jdbcTemplate.queryForObject("SELECT stock FROM product WHERE id = ?", Integer.class, savedProduct.getId());
        assertThat(result).isEqualTo(10);
    }

    @Test
    public void updateProductStockWithDeletedProduct() throws Exception {
        //given
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");
        Product savedProduct = saveProduct(savedStore, 1);
        jdbcTemplate.update("UPDATE product SET deleted_at = ? WHERE id = ?", LocalDateTime.now(), savedProduct.getId());
        MockHttpSession session = login("testuser123");

        //when
        mockMvc.perform(patch("/stores/me/products/{productId}/stock", savedProduct.getId())
                        .session(session)
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "delta" : 10
                                }
                                """))
                .andDo(print())
                //then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
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

    private @NonNull Product saveProduct(Store store,int num) {
        return productRepository.save(new Product(store, "testProduct" + num, "testDescription" + num, 1000 * num, ProductCategory.BOOK));
    }

    private @NonNull Store saveStore(AppUser savedUser,String storeName) {
        return storeRepository.save(new Store(savedUser.getId(), storeName));
    }

    private @NonNull AppUser saveUser(String username) {
        return appUserRepository.save(new AppUser(username, passwordEncoder.encode(username)));
    }
}
