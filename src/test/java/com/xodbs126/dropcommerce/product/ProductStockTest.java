package com.xodbs126.dropcommerce.product;

import com.xodbs126.dropcommerce.IntegrationTest;
import com.xodbs126.dropcommerce.appuser.entity.AppUser;
import com.xodbs126.dropcommerce.appuser.repository.AppUserRepository;
import com.xodbs126.dropcommerce.product.entity.Product;
import com.xodbs126.dropcommerce.product.entity.ProductCategory;
import com.xodbs126.dropcommerce.product.repository.ProductRepository;
import com.xodbs126.dropcommerce.store.entity.Store;
import com.xodbs126.dropcommerce.store.repository.StoreRepository;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

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
        Store savedStore = saveStore(savedUser);
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
        Assertions.assertThat(targetStock).isEqualTo(20);
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

    private @NonNull Store saveStore(AppUser savedUser) {
        return storeRepository.save(new Store(savedUser.getId(), "testStore123"));
    }

    private @NonNull AppUser saveUser(String username) {
        return appUserRepository.save(new AppUser(username, passwordEncoder.encode(username)));
    }
}
