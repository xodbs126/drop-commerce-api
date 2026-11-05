package com.xodbs126.dropcommerce.global.interceptor;

import com.xodbs126.dropcommerce.appuser.entity.AppUser;
import com.xodbs126.dropcommerce.appuser.repository.AppUserRepository;
import com.xodbs126.dropcommerce.global.exception.GlobalException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.xodbs126.dropcommerce.global.exception.errorcode.CommonErrorCode.ADMIN_REQUIRED;
import static com.xodbs126.dropcommerce.global.exception.errorcode.CommonErrorCode.LOGIN_REQUIRED;
import static com.xodbs126.dropcommerce.global.session.SessionConst.SESSION_KEY_APP_USER_ID;

@Component
@RequiredArgsConstructor
public class AdminInterceptor implements HandlerInterceptor {

    private final AppUserRepository appUserRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        Long appUserId = (Long) session.getAttribute(SESSION_KEY_APP_USER_ID);

        AppUser loginUser = appUserRepository.findById(appUserId).orElseThrow(() -> new GlobalException(LOGIN_REQUIRED));

        if (!loginUser.isAdmin()) {
            throw new GlobalException(ADMIN_REQUIRED);
        }

        return true;
    }
}
