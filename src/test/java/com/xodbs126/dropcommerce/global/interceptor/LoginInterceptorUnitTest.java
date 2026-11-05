package com.xodbs126.dropcommerce.global.interceptor;

import com.xodbs126.dropcommerce.global.exception.GlobalException;
import com.xodbs126.dropcommerce.global.session.SessionConst;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import static com.xodbs126.dropcommerce.global.exception.errorcode.CommonErrorCode.LOGIN_REQUIRED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LoginInterceptorUnitTest {

    private final MockHttpSession mockHttpSession = new MockHttpSession();

    @Test
    public void withoutAppUserIdInSessionThrowsException() {
        //given
        LoginInterceptor interceptor = new LoginInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(mockHttpSession);
        MockHttpServletResponse response = new MockHttpServletResponse();

        //when
        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                //then
                .isInstanceOf(GlobalException.class)
                .extracting("errorCode")
                .isEqualTo(LOGIN_REQUIRED);
    }

    @Test
    public void withAppUserIdInSessionReturnsTrue() {
        //given
        LoginInterceptor interceptor = new LoginInterceptor();
        mockHttpSession.setAttribute(SessionConst.SESSION_KEY_APP_USER_ID, 1L);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(mockHttpSession);
        MockHttpServletResponse response = new MockHttpServletResponse();

        //when
        boolean result = interceptor.preHandle(request, response, new Object());

        //then
        assertThat(result).isTrue();
    }
}
