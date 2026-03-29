package com.omra.platform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omra.platform.exception.ErrorResponse;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.service.SubscriptionGateService;
import com.omra.platform.util.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

/**
 * Après authentification JWT : refuse les requêtes API des comptes d’agence si l’abonnement
 * n’est plus valide (session encore ouverte côté client).
 */
@Component
@RequiredArgsConstructor
public class SubscriptionEnforcementFilter extends OncePerRequestFilter {

    private final SubscriptionGateService subscriptionGateService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && path.startsWith(ctx)) {
            path = path.substring(ctx.length());
        }
        if (path.isEmpty()) {
            path = "/";
        }

        if (isExemptPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null
                || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken) {
            filterChain.doFilter(request, response);
            return;
        }

        if (TenantContext.isSuperAdmin() || TenantContext.isAdmin()) {
            filterChain.doFilter(request, response);
            return;
        }

        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            subscriptionGateService.assertAgencyUsersMayAuthenticate(agencyId);
        } catch (ForbiddenException ex) {
            writeForbidden(request, response, ex);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private static boolean isExemptPath(String path) {
        return path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/logout")
                || path.startsWith("/api/auth/refresh")
                || path.startsWith("/api/public/")
                || path.startsWith("/api/admin/")
                || path.startsWith("/api/mobile/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/swagger-ui.html");
    }

    private void writeForbidden(HttpServletRequest request, HttpServletResponse response, ForbiddenException ex)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpServletResponse.SC_FORBIDDEN)
                .error("Forbidden")
                .message(ex.getMessage())
                .code(ex.getCode())
                .path(request.getRequestURI())
                .build();
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
