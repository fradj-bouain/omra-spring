package com.omra.platform.config;

import com.omra.platform.interceptor.AuditLogInterceptor;
import com.omra.platform.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuditLogService auditLogService;
    private final StorageProperties storageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path root = localUploadsRoot();
        String location = root.toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }

    private Path localUploadsRoot() {
        String configured = storageProperties.getLocalPath();
        if (configured == null || configured.isBlank()) {
            return Paths.get(System.getProperty("user.home", "."), "omra-uploads").toAbsolutePath().normalize();
        }
        return Paths.get(configured).toAbsolutePath().normalize();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuditLogInterceptor(auditLogService))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/refresh",
                        "/api/admin/auth/login",
                        "/api/admin/auth/refresh",
                        "/api/admin/auth/logout",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/actuator/**"
                );
    }

    /** Register content-caching filter so it runs before the security chain and wraps request/response for audit. */
    @Bean
    public FilterRegistrationBean<ContentCachingFilter> contentCachingFilterRegistration() {
        FilterRegistrationBean<ContentCachingFilter> registration = new FilterRegistrationBean<>(new ContentCachingFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/api/*");
        return registration;
    }
}
