package com.xodbs126.dropcommerce.global.config;

import com.xodbs126.dropcommerce.global.interceptor.OriginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class OriginInterceptorConfig implements WebMvcConfigurer {

    private final OriginInterceptor originInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(originInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/error")
                .order(0);
    }
}
