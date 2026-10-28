package com.xodbs126.dropcommerce.appuser;


import com.xodbs126.dropcommerce.IntegrationTest;
import com.xodbs126.dropcommerce.appuser.entity.AppUser;
import com.xodbs126.dropcommerce.appuser.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@TestPropertySource(properties = {
        "logging.level.org.hibernate.SQL=debug",
        "logging.level.org.hibernate.orm.jdbc.bind=trace"
})

@AutoConfigureMockMvc
public class AppUserRegisterTest extends IntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    public void saveDuplicatedUsername() throws Exception {
        //given
        AppUser appUser = new AppUser("testuser1", "testuser1");
        appUserRepository.save(appUser);


        //when
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "testuser1",
                                  "password": "testuser1"
                                }
                                """))
                .andDo(print())
                //then
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USERNAME_DUPLICATED"));

        assertThat(appUserRepository.count()).isEqualTo(1L);

    }

    @Test
    public void requestNullUsernameAndPassword() throws Exception {
        //given, when
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andDo(print())
                //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors", hasSize(2)))
                .andExpect(jsonPath("$.errors[?(@.field == 'username')].reason", hasItem("REQUIRED")))
                .andExpect(jsonPath("$.errors[?(@.field == 'password')].reason", hasItem("REQUIRED")));
    }

}
