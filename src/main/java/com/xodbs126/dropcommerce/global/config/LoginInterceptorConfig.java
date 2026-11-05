package com.xodbs126.dropcommerce.global.config;

import com.xodbs126.dropcommerce.global.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class LoginInterceptorConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final String[] excludePatterns = {"/users", "/login", "/error", "/drops", "/drops/*"};

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(excludePatterns);
    }
}
