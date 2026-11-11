package com.xodbs126.dropcommerce.product;

import com.xodbs126.dropcommerce.IntegrationTest;
import com.xodbs126.dropcommerce.appuser.entity.AppUser;
import com.xodbs126.dropcommerce.appuser.repository.AppUserRepository;
import com.xodbs126.dropcommerce.product.entity.Product;
import com.xodbs126.dropcommerce.product.entity.ProductCategory;
import com.xodbs126.dropcommerce.product.repository.ProductRepository;
import com.xodbs126.dropcommerce.store.entity.Store;
import com.xodbs126.dropcommerce.store.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class ProductDetailTest extends IntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void getProductSuccess() throws Exception {
        //given
        AppUser savedUser = saveAppUser();
        Store savedStore = saveStore(savedUser);
        Product savedProduct = saveProduct(savedStore);

        MockHttpSession session = login();

        //when
        mockMvc.perform(get("/stores/me/products/{productId}", savedProduct.getId())
                        .session(session))
                //then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(savedProduct.getId()))
                .andExpect(jsonPath("$.name").value("testProduct"))
                .andExpect(jsonPath("$.description").value("testDescription"))
                .andExpect(jsonPath("$.category").value("FOOD"))
                .andExpect(jsonPath("$.price").value(1000))
                .andExpect(jsonPath("$.stock").value(0));
    }

    @Test
    public void getProductWithNotExistedProduct() throws Exception {
        //given
        AppUser savedUser = saveAppUser();
        saveStore(savedUser);

        MockHttpSession session = login();

        //when
        mockMvc.perform(get("/stores/me/products/{productId}", 999L)
                        .session(session))
                //then
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    public void getProductWithNotStoreOwner() throws Exception {
        //given
        AppUser savedUser = saveAppUser();
        AppUser anotherUser = appUserRepository.save(new AppUser("anotherUser123", passwordEncoder.encode("anotherUser123")));

        saveStore(savedUser);
        Store anotherStore = saveStore(anotherUser);

        Product product = saveProduct(anotherStore);

        MockHttpSession session = login();

        //when
        mockMvc.perform(get("/stores/me/products/{productId}", product.getId())
                        .session(session))
                //then
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    public void getProductWithoutLogin() throws Exception {
        //given
        AppUser savedUser = saveAppUser();
        Product product = saveProduct(saveStore(savedUser));

        //when
        mockMvc.perform(get("/stores/me/products/{productId}", product.getId()))
                //then
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("LOGIN_REQUIRED"));
    }

    @Test
    public void getProductWithoutStore() throws Exception {
        //given
        AppUser savedUser = saveAppUser();
        AppUser anotherUser = appUserRepository.save(new AppUser("anotherUser123", passwordEncoder.encode("anotherUser123")));

        Store anotherStore = saveStore(anotherUser);

        Product product = saveProduct(anotherStore);

        MockHttpSession session = login();

        //when
        mockMvc.perform(get("/stores/me/products/{productId}", product.getId())
                        .session(session))
                //then
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("STORE_NOT_FOUND"));
    }

    private Product saveProduct(Store savedStore) {
        return productRepository.save(new Product(savedStore, "testProduct", "testDescription", 1000, ProductCategory.FOOD));
    }

    private Store saveStore(AppUser savedUser) {
        return storeRepository.save(new Store(savedUser.getId(), "testStore"));
    }

    private AppUser saveAppUser() {
        return appUserRepository.save(new AppUser("testuser123", passwordEncoder.encode("testuser123")));
    }

    private MockHttpSession login() throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "username" : "testuser123",
                                "password" : "testuser123"
                                }
                                """))
                .andExpect(status().isNoContent())
                .andReturn();

        return (MockHttpSession) result.getRequest().getSession(false);
    }
}
