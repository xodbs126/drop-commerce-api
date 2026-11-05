package com.xodbs126.dropcommerce.global.interceptor;

import com.xodbs126.dropcommerce.ServerIntegrationTest;
import com.xodbs126.dropcommerce.appuser.entity.AppUser;
import com.xodbs126.dropcommerce.appuser.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.client.ExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

public class AdminInterceptorTest extends ServerIntegrationTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void accessAdminWithNoLogin() {
        //given
        //when
        restTestClient.get()
                .uri("/admin")
                .exchange()
                //then
                .expectStatus().isUnauthorized()
                .expectBody().consumeWith(System.out::println)
                .jsonPath("$.code").isEqualTo("LOGIN_REQUIRED");
    }

    @Test
    public void accessAdminWithNoAdmin() {
        //given
        appUserRepository.save(new AppUser("testuser123", passwordEncoder.encode("testuser123")));
        ExchangeResult result = restTestClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                        "username" : "testuser123",
                        "password" : "testuser123"
                        }
                        """)
                .exchange()
                .expectStatus().isNoContent()
                .returnResult();
        String jsessionid = result.getResponseCookies().getFirst("JSESSIONID").getValue();

        //when
        restTestClient.get()
                .uri("/admin")
                .cookie("JSESSIONID", jsessionid)
                .exchange()
                //then
                .expectStatus().isForbidden()
                .expectBody().consumeWith(System.out::println)
                .jsonPath("$.code").isEqualTo("ADMIN_REQUIRED");
    }

    @Test
    public void accessAdminWithAdmin() {
        //given
        AppUser appUser = new AppUser("testuser123", passwordEncoder.encode("testuser123"));
        appUser.promoteToAdmin();
        appUserRepository.save(appUser);

        ExchangeResult result = restTestClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                            {
                            "username" : "testuser123",
                            "password" : "testuser123"
                            }
                        """)
                .exchange()
                .expectStatus().isNoContent()
                .returnResult();

        String jsessionid = result
                .getResponseCookies().getFirst("JSESSIONID").getValue();

        //when
        restTestClient.get()
                .uri("/admin")
                .cookie("JSESSIONID", jsessionid)
                .exchange()
                //then
                .expectStatus().isNoContent()
                .expectBody().consumeWith(System.out::println);

    }
}
