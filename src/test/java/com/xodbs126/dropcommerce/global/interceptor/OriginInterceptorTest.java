package com.xodbs126.dropcommerce.global.interceptor;

import com.xodbs126.dropcommerce.ServerIntegrationTest;
import com.xodbs126.dropcommerce.appuser.entity.AppUser;
import com.xodbs126.dropcommerce.appuser.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.client.RestTestClient;

public class OriginInterceptorTest extends ServerIntegrationTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void withoutOriginReturnsForbidden() {
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
                .expectStatus().isForbidden()
                .expectBody().consumeWith(System.out::println)
                .jsonPath("$.code").isEqualTo("ORIGIN_NOT_ALLOWED");
    }

    @Test
    public void withDisallowedOriginReturnsForbidden() {
        //given
        appUserRepository.save(new AppUser("testuser123", passwordEncoder.encode("testuser123")));

        //when
        restTestClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Origin", "http://evil.example")
                .body("""
                        {
                        "username" : "testuser123",
                        "password" : "testuser123"
                        }
                        """)
                .exchange()
                //then
                .expectStatus().isForbidden()
                .expectBody().consumeWith(System.out::println)
                .jsonPath("$.code").isEqualTo("ORIGIN_NOT_ALLOWED");
    }

    @Test
    public void withAllowedOriginPassesOriginCheck() {
        //given, when
        restTestClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Origin", "http://localhost:3000")
                .body("{}")
                .exchange()
                //then
                .expectStatus().isBadRequest()
                .expectBody().consumeWith(System.out::println)
                .jsonPath("$.code").isEqualTo("INVALID_INPUT");
    }

    @Test
    public void getWithoutOriginSkipsOriginCheck() {
        //given, when
        restTestClient.get()
                .uri("/test/auth")
                .exchange()
                //then
                .expectStatus().isUnauthorized()
                .expectBody().consumeWith(System.out::println)
                .jsonPath("$.code").isEqualTo("LOGIN_REQUIRED");
    }
}
