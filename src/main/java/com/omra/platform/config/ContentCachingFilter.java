package com.omra.platform.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.List;

/**
 * Wraps request and response in content-caching wrappers so the body can be read
 * after the request (e.g. in AuditLogInterceptor) without consuming the stream.
 */
public class ContentCachingFilter extends OncePerRequestFilter {

    private static final List<String> SKIP_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/admin/auth/login",
            "/api/admin/auth/refresh",
            "/api/admin/auth/logout",
            "/v3/api-docs",
            "/swagger-ui",
            "/actuator",
            "/health"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/") && !path.startsWith("/v3/") && !path.startsWith("/swagger") && !path.startsWith("/actuator")) {
            return true;
        }
        for (String skip : SKIP_PATHS) {
            if (path.startsWith(skip)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            wrappedResponse.copyBodyToResponse();
        }
    }
}
