package com.xodbs126.dropcommerce.appuser;

import com.xodbs126.dropcommerce.ServerIntegrationTest;
import com.xodbs126.dropcommerce.appuser.entity.AppUser;
import com.xodbs126.dropcommerce.appuser.repository.AppUserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.client.ExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
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

    @Test
    public void checkChangeSession() {
        //given
        // encode 하는 이유: 평문으로 저장하면 로그인이 401로 실패해서, 세션 ID를 바꾸는 코드(changeSessionId)까지 가지 못한다
        appUserRepository.save(new AppUser("testuser123", passwordEncoder.encode("testuser123")));
        // 첫 로그인으로 세션을 만들고, 그 세션 ID를 꺼내 둔다
        ExchangeResult firstResult = restTestClient.post()
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
                .expectBody().consumeWith(System.out::println)
                .returnResult();

        String firstJSESSIONID = firstResult.getResponseCookies().getFirst("JSESSIONID").getValue();

        //when
        ExchangeResult secondResult = restTestClient.post()
                .uri("/login")
                // 첫 세션 ID를 실어야 서버가 기존 세션을 찾아 changeSessionId()를 실행한다 (안 실으면 그냥 새 세션이라 ID가 늘 다름)
                .cookie("JSESSIONID", firstJSESSIONID)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                          {
                          "username" : "testuser123",
                          "password" : "testuser123"
                          }
                        """)
                .exchange()
                .expectStatus().isNoContent()
                // ID가 안 바뀌면 Set-Cookie가 오지 않는다 → 아래 getValue()의 NPE 대신 여기서 실패하게 먼저 검사
                .expectHeader().valueMatches("Set-Cookie", "JSESSIONID=.*")
                .expectBody().consumeWith(System.out::println)
                .returnResult();

        String secondJSESSIONID = secondResult.getResponseCookies().getFirst("JSESSIONID").getValue();

        //then
        assertThat(firstJSESSIONID).isNotEqualTo(secondJSESSIONID);

    }

}
