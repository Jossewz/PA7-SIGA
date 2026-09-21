package com.siga.siga_iea.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final PermisoModuloInterceptor permisoModuloInterceptor;

    public WebConfig(PermisoModuloInterceptor permisoModuloInterceptor) {
        this.permisoModuloInterceptor = permisoModuloInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permisoModuloInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/logout",
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/icons/**",
                        "/fonts/**",
                        "/favicon.ico",
                        "/error"
                );
    }
}
