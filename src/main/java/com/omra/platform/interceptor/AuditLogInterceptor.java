package com.omra.platform.interceptor;

import com.omra.platform.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class AuditLogInterceptor implements HandlerInterceptor {

    private final AuditLogService auditLogService;

    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception ex) {
        if (!(request instanceof ContentCachingRequestWrapper)) {
            return;
        }
        ContentCachingRequestWrapper req = (ContentCachingRequestWrapper) request;
        String requestBody = getContent(req.getContentAsByteArray());
        String responseBody = null;
        if (response instanceof ContentCachingResponseWrapper) {
            responseBody = getContent(((ContentCachingResponseWrapper) response).getContentAsByteArray());
        }
        String ip = resolveClientIp(request);
        auditLogService.saveFromRequest(
                request.getRequestURI(),
                request.getMethod(),
                requestBody,
                responseBody,
                response.getStatus(),
                ip
        );
    }

    private static String getContent(byte[] buf) {
        if (buf == null || buf.length == 0) return null;
        return new String(buf, StandardCharsets.UTF_8);
    }

    private static String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
