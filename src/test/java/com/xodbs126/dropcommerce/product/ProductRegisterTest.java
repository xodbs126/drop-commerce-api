package com.xodbs126.dropcommerce.product;

import com.xodbs126.dropcommerce.IntegrationTest;
import com.xodbs126.dropcommerce.appuser.entity.AppUser;
import com.xodbs126.dropcommerce.appuser.repository.AppUserRepository;
import com.xodbs126.dropcommerce.product.repository.ProductRepository;
import com.xodbs126.dropcommerce.store.entity.Store;
import com.xodbs126.dropcommerce.store.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class ProductRegisterTest extends IntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    public void registerProductSuccess() throws Exception {
        //given
        saveUserWithStore();
        MockHttpSession session = login();

        //when
        mockMvc.perform(post("/stores/me/products")
                        .header("Origin", "http://localhost:3000")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "name" : "testProduct123",
                                "description" : "testProduct123",
                                "category" : "FOOD",
                                "price" : 1000
                                }
                                """
                        )).andDo(print())
                //then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").isNumber());

        assertThat(productRepository.count()).isEqualTo(1L);
        assertThat(productRepository.findAll().getFirst().getStock()).isEqualTo(0);
    }

    @Test
    public void registerProductWithEmptyDescription() throws Exception {
        //given
        saveUserWithStore();
        MockHttpSession session = login();

        //when
        mockMvc.perform(post("/stores/me/products")
                        .header("Origin", "http://localhost:3000")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "name" : "testProduct123",
                                "description" : "",
                                "category" : "FOOD",
                                "price" : 1000
                                }
                                """
                        )).andDo(print())
                //then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").isNumber());

        assertThat(productRepository.count()).isEqualTo(1L);
        assertThat(productRepository.findAll().getFirst().getStock()).isEqualTo(0);
    }

    @Test
    public void registerProductWithoutStore() throws Exception {
        //given
        saveUser();
        MockHttpSession session = login();

        //when
        mockMvc.perform(post("/stores/me/products")
                        .header("Origin", "http://localhost:3000")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "name" : "testProduct123",
                                "description" : "testProduct123",
                                "category" : "FOOD",
                                "price" : 1000
                                }
                                """))
                //then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("STORE_NOT_FOUND"));

        assertThat(productRepository.count()).isEqualTo(0L);
    }

    @Test
    public void registerProductWithoutLogin() throws Exception {
        //given
        //when
        mockMvc.perform(post("/stores/me/products")
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "name" : "testProduct123",
                                "description" : "testProduct123",
                                "category" : "FOOD",
                                "price" : 1000
                                }
                                """))
                .andDo(print())
                //then
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("LOGIN_REQUIRED"));
    }

    @Test
    public void registerProductWithoutCategory() throws Exception {
        //given
        saveUserWithStore();
        MockHttpSession session = login();

        //when
        mockMvc.perform(post("/stores/me/products")
                        .header("Origin", "http://localhost:3000")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "name" : "testProduct123",
                                "description" : "testProduct123",
                                "price" : 1000
                                }
                                """
                        )).andDo(print())
                //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field=='category')].reason", hasItem("REQUIRED")));
    }

    @Test
    public void registerProductWithoutName() throws Exception {
        //given
        saveUserWithStore();
        MockHttpSession session = login();

        //when
        mockMvc.perform(post("/stores/me/products")
                        .header("Origin", "http://localhost:3000")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "description" : "testProduct123",
                                "category" : "FOOD",
                                "price" : 1000
                                }
                                """
                        )).andDo(print())
                //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field=='name')].reason", hasItem("REQUIRED")));
    }

    @Test
    public void registerProductWithOverlengthName() throws Exception {
        //given
        saveUserWithStore();
        MockHttpSession session = login();

        //when
        mockMvc.perform(post("/stores/me/products")
                        .header("Origin", "http://localhost:3000")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "name": "testProduct1234testProduct1234testProduct1234testProduct1234testProduct1234testProduct1234testProduct1234testProduct1234testProduct1234testProduct12341",
                                "description" : "testProduct123",
                                "category" : "FOOD",
                                "price" : 1000
                                }
                                """
                        )).andDo(print())
                //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field=='name')].reason", hasItem("OUT_OF_RANGE")));
    }

    @Test
    public void registerProductWithoutDescription() throws Exception {
        //given
        saveUserWithStore();
        MockHttpSession session = login();

        //when
        mockMvc.perform(post("/stores/me/products")
                        .header("Origin", "http://localhost:3000")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "name" : "testProduct123",
                                "description" : null,
                                "category" : "FOOD",
                                "price" : 1000
                                }
                                """
                        )).andDo(print())
                //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field=='description')].reason", hasItem("REQUIRED")));
    }

    @Test
    public void registerProductWithPriceZero() throws Exception {
        //given
        saveUserWithStore();
        MockHttpSession session = login();

        //when
        mockMvc.perform(post("/stores/me/products")
                        .header("Origin", "http://localhost:3000")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "name" : "testProduct123",
                                "description" : "testProduct123",
                                "category" : "FOOD",
                                "price" : 0
                                }
                                """
                        )).andDo(print())
                //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field=='price')].reason", hasItem("MUST_BE_POSITIVE")));
    }

    @Test
    public void registerProductWithNotInCategory() throws Exception {
        //given
        saveUserWithStore();
        MockHttpSession session = login();

        //when
        mockMvc.perform(post("/stores/me/products")
                        .header("Origin", "http://localhost:3000")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "name" : "testProduct123",
                                "description" : "testProduct123",
                                "category" : "SHOES",
                                "price" : 1000
                                }
                                """))
                .andDo(print())
                //then
                .andExpect(status().isBadRequest());
    }

    private AppUser saveUser() {
        return appUserRepository.save(new AppUser("testuser123", passwordEncoder.encode("testuser123")));
    }

    private void saveUserWithStore() {
        AppUser savedUser = saveUser();
        storeRepository.save(new Store(savedUser.getId(), "testStore"));
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
