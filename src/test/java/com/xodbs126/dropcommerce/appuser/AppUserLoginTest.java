package com.xodbs126.dropcommerce.appuser;

import com.xodbs126.dropcommerce.IntegrationTest;
import com.xodbs126.dropcommerce.appuser.dto.AppUserLoginRequestDto;
import com.xodbs126.dropcommerce.appuser.dto.AppUserRegisterRequestDto;
import com.xodbs126.dropcommerce.appuser.entity.AppUser;
import com.xodbs126.dropcommerce.appuser.repository.AppUserRepository;
import com.xodbs126.dropcommerce.appuser.service.AppUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
public class AppUserLoginTest extends IntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void checkSetCookieInResponse() throws Exception {
        //given
        appUserService.register(new AppUserRegisterRequestDto("testuser123", "testuser123"));

        //when
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "username" : "testuser123",
                                "password" : "testuser123"
                                }
                                """))
                .andDo(MockMvcResultHandlers.print())
                //then

                .andExpect(status().isNoContent());


    }
}
