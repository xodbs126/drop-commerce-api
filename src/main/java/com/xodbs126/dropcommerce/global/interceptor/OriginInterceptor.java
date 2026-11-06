package com.xodbs126.dropcommerce.global.interceptor;

import com.xodbs126.dropcommerce.global.exception.GlobalException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Objects;

import static com.xodbs126.dropcommerce.global.exception.errorcode.CommonErrorCode.ORIGIN_NOT_ALLOWED;

@Component
public class OriginInterceptor implements HandlerInterceptor {

    @Value("${app.allowed-origins}")
    private List<String> allowedOrigins;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();

        if (Objects.equals(method, "GET") || Objects.equals(method, "HEAD") || Objects.equals(method, "OPTIONS")) {
            return true;
        }

        boolean isAllowed = false;
        String origin = request.getHeader("Origin");
        for (String allowedOrigin : allowedOrigins) {
            if (allowedOrigin.equals(origin)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new GlobalException(ORIGIN_NOT_ALLOWED);
        }

        return true;
    }
}
