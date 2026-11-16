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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class ProductListTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Test
    public void getProductListSuccess() throws Exception {
        //given
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");

        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);

        MockHttpSession session = login(savedUser.getUsername());

        //when
        mockMvc.perform(get("/stores/me/products")
                        .session(session))
                //then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productId").value(savedProduct2.getId()))
                .andExpect(jsonPath("$.content[1].productId").value(savedProduct1.getId()));
    }

    @Test
    public void getProductListWithoutDeleted() throws Exception {
        //given
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");

        Product savedProduct1 = saveProduct(savedStore, 1);
        Product savedProduct2 = saveProduct(savedStore, 2);

        jdbcTemplate.update("UPDATE product SET deleted_at = ? WHERE id = ?", LocalDateTime.now(), savedProduct1.getId());

        MockHttpSession session = login(savedUser.getUsername());

        //when
        mockMvc.perform(get("/stores/me/products")
                        .session(session))
                //then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].productId").value(savedProduct2.getId()));
    }

    @Test
    public void getProductListExceptOtherStore() throws Exception {
        //given
        AppUser savedUser = saveUser("testuser123");
        AppUser anotherUser = saveUser("testuser321");

        Store savedStore = saveStore(savedUser, "testStore1");
        Store anotherStore = saveStore(anotherUser, "testStore2");

        Product savedProduct = saveProduct(savedStore, 1);
        saveProduct(anotherStore, 1);

        MockHttpSession session = login(savedUser.getUsername());

        //when
        mockMvc.perform(get("/stores/me/products")
                        .session(session))
                //then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].productId").value(savedProduct.getId()));
    }

    @Test
    public void getProductListWithOversize() throws Exception {
        //given
        AppUser savedUser = saveUser("testuser123");
        saveStore(savedUser, "testStore1");

        MockHttpSession session = login(savedUser.getUsername());

        //when
        mockMvc.perform(get("/stores/me/products")
                        .session(session)
                        .param("size", "101"))
                //then
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[?(@.field=='size')].reason", hasItem("OUT_OF_RANGE")));
    }

    @Test
    public void getProductListWithoutPagingParameters() throws Exception {
        //given
        AppUser savedUser = saveUser("testuser123");
        saveStore(savedUser, "testStore1");

        MockHttpSession session = login(savedUser.getUsername());

        //when
        mockMvc.perform(get("/stores/me/products")
                        .session(session))
                //then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    public void getProductListWithNextPage() throws Exception {
        //given
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");

        for (int i = 1; i <= 21; i++) {
            saveProduct(savedStore, i);
        }

        MockHttpSession session = login(savedUser.getUsername());

        //when
        mockMvc.perform(get("/stores/me/products")
                        .session(session))
                //then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(20))
                .andExpect(jsonPath("$.hasNext").value(true));

        //when
        mockMvc.perform(get("/stores/me/products")
                        .session(session)
                        .param("page", "1"))
                //then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    public void getProductListWithFullLastPage() throws Exception {
        //given
        AppUser savedUser = saveUser("testuser123");
        Store savedStore = saveStore(savedUser, "testStore1");

        for (int i = 1; i <= 20; i++) {
            saveProduct(savedStore, i);
        }

        MockHttpSession session = login(savedUser.getUsername());

        //when
        mockMvc.perform(get("/stores/me/products")
                        .session(session))
                //then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(20))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    private Product saveProduct(Store savedStore, int n) {
        return productRepository.save(new Product(savedStore, "testProduct" + n, "testDescription" + n, n * 1000, ProductCategory.BOOK));
    }

    private Store saveStore(AppUser savedUser, String storeName) {
        return storeRepository.save(new Store(savedUser.getId(), storeName));
    }

    private AppUser saveUser(String username) {
        return appUserRepository.save(new AppUser(username, passwordEncoder.encode(username)));
    }

    private MockHttpSession login(String username) throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/login")
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "username" : "%s",
                                "password" : "%s"
                                }
                                """.formatted(username, username)))
                .andExpect(status().isNoContent())
                .andReturn().getRequest().getSession(false);
    }
}
