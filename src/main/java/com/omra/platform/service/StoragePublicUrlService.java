package com.omra.platform.service;

import com.omra.platform.config.StorageProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Construit une URL absolue pour les fichiers servis par l’application (ex. /uploads/...).
 * Les URLs MinIO/S3 complètes sont renvoyées telles quelles.
 */
@Service
@RequiredArgsConstructor
public class StoragePublicUrlService {

    private final StorageProperties storageProperties;

    public String toAbsoluteUrl(HttpServletRequest request, String storedPathOrUrl) {
        if (!StringUtils.hasText(storedPathOrUrl)) {
            return storedPathOrUrl;
        }
        String s = storedPathOrUrl.trim();
        if (s.startsWith("http://") || s.startsWith("https://")) {
            return s;
        }
        String base = storageProperties.getPublicBaseUrl();
        if (!StringUtils.hasText(base)) {
            base = inferRequestBaseUrl(request);
        } else {
            base = base.trim().replaceAll("/+$", "");
        }
        if (!s.startsWith("/")) {
            s = "/" + s;
        }
        return base + s;
    }

    private static String inferRequestBaseUrl(HttpServletRequest request) {
        String scheme = headerOr(request, "X-Forwarded-Proto", request.getScheme());
        String hostHeader = request.getHeader("X-Forwarded-Host");
        if (StringUtils.hasText(hostHeader)) {
            return scheme + "://" + hostHeader.trim();
        }
        int port = request.getServerPort();
        String host = request.getServerName();
        boolean defPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
        if (defPort) {
            return scheme + "://" + host;
        }
        return scheme + "://" + host + ":" + port;
    }

    private static String headerOr(HttpServletRequest request, String name, String fallback) {
        String v = request.getHeader(name);
        return StringUtils.hasText(v) ? v.trim() : fallback;
    }
}
