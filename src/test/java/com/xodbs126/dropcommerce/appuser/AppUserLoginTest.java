package com.xodbs126.dropcommerce.appuser;

import com.xodbs126.dropcommerce.ServerIntegrationTest;
import com.xodbs126.dropcommerce.appuser.entity.AppUser;
import com.xodbs126.dropcommerce.appuser.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


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

    @Test
    public void loginNoUsernameInDB() {
        //given when
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
                .expectStatus().isUnauthorized()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.code").isEqualTo("LOGIN_FAILED")
                .jsonPath("$.message").isEqualTo("로그인에 실패했습니다.");
    }

    @Test
    public void loginWrongPassword() {
        //given
        // matches는 DB값을 BCrypt 해시로 보고 비교하므로, 평문으로 저장하면 비밀번호가 맞아도 인증에 실패한다 → 이 테스트가 틀린 비밀번호를 제대로 검증해서 401이 나는지 검사하지 못한다.
        appUserRepository.save(new AppUser("testuser123", passwordEncoder.encode("testuser123")));

        //when
        restTestClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                        "username" : "testuser123",
                        "password" : "testuser1234"
                        }
                      """)
                .exchange()
                //then
                .expectStatus().isUnauthorized()
                .expectBody().consumeWith(System.out::println)
                .jsonPath("$.code").isEqualTo("LOGIN_FAILED")
                .jsonPath("$.message").isEqualTo("로그인에 실패했습니다.");

    }

    @Test
    public void loginBlankUsernameAndPassword() {
        //given: 저장 안 함. @Valid에서 막혀 DB까지 가지 않음

        //when
        restTestClient.post()
                .uri("/login")
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
                .jsonPath("$.code").isEqualTo("INVALID_INPUT")
                .jsonPath("$.errors[*].field").value((List<String> fields) ->
                        assertThat(fields).containsExactlyInAnyOrder("username", "password"))
                .jsonPath("$.errors[*].reason").isEqualTo(List.of("REQUIRED", "REQUIRED"));
    }

}
