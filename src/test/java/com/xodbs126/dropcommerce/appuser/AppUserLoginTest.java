package com.xodbs126.dropcommerce.appuser;

import com.xodbs126.dropcommerce.ServerIntegrationTest;
import com.xodbs126.dropcommerce.appuser.entity.AppUser;
import com.xodbs126.dropcommerce.appuser.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.client.RestTestClient;


public class AppUserLoginTest extends ServerIntegrationTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void checkSetCookieInResponse() {
        //given
        appUserRepository.save(new AppUser("testuser123", passwordEncoder.encode("testuser123")));

        //when
        restTestClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                        "username" : "testuser123",
                        "password" : "testuser123"
                        }
                        """)
                .exchange()
                //then
                .expectStatus().isNoContent()
                .expectHeader().valueMatches("Set-Cookie", "JSESSIONID=.*");

    }
}
