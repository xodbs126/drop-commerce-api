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

public class LoginInterceptorTest extends ServerIntegrationTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void withoutCookieReturnsUnauthorized() {
        //given
        //when
        restTestClient.get()
                .uri("/test/auth")
                .exchange()
                //then
                .expectStatus().isUnauthorized()
                .expectBody().consumeWith(System.out::println)
                .jsonPath("$.code").isEqualTo("LOGIN_REQUIRED");
    }

    @Test
    public void withLoginSessionCookiePasses() {
        //given
        appUserRepository.save(new AppUser("testuser123", passwordEncoder.encode("testuser123")));

        ExchangeResult result = restTestClient.post()
                .uri("/login")
                .header("Origin", "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                        "username" : "testuser123",
                        "password" : "testuser123"
                        }
                        """)
                .exchange()
                .expectStatus().isNoContent()
                .expectHeader().valueMatches("Set-Cookie", "JSESSIONID=.*")
                .returnResult();

        String jsessionid = result.getResponseCookies().getFirst("JSESSIONID").getValue();

        //when
        restTestClient.get()
                .uri("/test/auth")
                .cookie("JSESSIONID", jsessionid)
                .exchange()
                //then
                .expectStatus().isNoContent();
    }

    @Test
    public void withUnknownSessionIdReturnsUnauthorized() {
        //given
        //when
        restTestClient.get()
                .uri("/test/auth")
                .cookie("JSESSIONID", "FAKE")
                .exchange()
                //then
                .expectStatus().isUnauthorized()
                .expectBody().consumeWith(System.out::println)
                .jsonPath("$.code").isEqualTo("LOGIN_REQUIRED");
    }

    @Test
    public void errorResponseOnExcludedUriIsNotChangedToUnauthorized() {
        //given
        //when
        restTestClient.post()
                .uri("/login")
                .header("Origin", "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .body("")
                .exchange()
                //then
                .expectStatus().isBadRequest()
                .expectBody().consumeWith(System.out::println);
    }

    @Test
    public void excludedUriSkipsLoginCheck() {
        //given
        //when
        restTestClient.post()
                .uri("/login")
                .header("Origin", "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                        "username" : "",
                        "password" : ""
                        }
                        """)
                .exchange()
                //then
                .expectStatus().isBadRequest()
                .expectBody().consumeWith(System.out::println)
                .jsonPath("$.code").isEqualTo("INVALID_INPUT");
    }
}
