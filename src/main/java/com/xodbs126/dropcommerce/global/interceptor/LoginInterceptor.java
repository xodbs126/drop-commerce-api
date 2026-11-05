package com.xodbs126.dropcommerce.global.interceptor;

import com.xodbs126.dropcommerce.global.exception.GlobalException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.xodbs126.dropcommerce.global.exception.errorcode.CommonErrorCode.LOGIN_REQUIRED;
import static com.xodbs126.dropcommerce.global.session.SessionConst.SESSION_KEY_APP_USER_ID;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new GlobalException(LOGIN_REQUIRED);
        }
        Object attribute = session.getAttribute(SESSION_KEY_APP_USER_ID);

        if (attribute == null) {
            throw new GlobalException(LOGIN_REQUIRED);
        }

        return true;
    }
}
