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
        AppUser saveUser = appUserRepository.save(new AppUser("testuser123", passwordEncoder.encode("testuser123")));
        storeRepository.save(new Store(saveUser.getId(), "testStore"));

        MvcResult result = mockMvc.perform(post("/login")
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "username" : "testuser123",
                                "password" : "testuser123"
                                }
                                """))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);

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
}
