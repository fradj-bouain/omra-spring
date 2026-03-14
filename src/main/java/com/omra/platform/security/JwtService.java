package com.omra.platform.security;

import com.omra.platform.entity.enums.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Long userId, Long agencyId, String email, UserRole role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("agencyId", agencyId != null ? agencyId.toString() : null)
                .claim("email", email)
                .claim("role", role.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenValidity() * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    /** Access token for platform Admin (superadmin). Grants SUPER_ADMIN role for agency management. */
    public String generateAccessTokenForAdmin(Long adminId, String email) {
        return Jwts.builder()
                .subject("admin:" + adminId)
                .claim("admin", true)
                .claim("adminId", adminId.toString())
                .claim("email", email)
                .claim("role", UserRole.SUPER_ADMIN.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenValidity() * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject("refresh:" + userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenValidity() * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshTokenForAdmin(Long adminId) {
        return Jwts.builder()
                .subject("refresh:admin:" + adminId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenValidity() * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    public JwtClaims parseAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String subject = claims.getSubject();
        String email = claims.get("email", String.class);
        UserRole role = UserRole.valueOf(claims.get("role", String.class));

        if (subject != null && subject.startsWith("admin:")) {
            Long adminId = Long.parseLong(subject.substring(6));
            return new JwtClaims(null, null, email, role, true, adminId);
        }

        Long userId = Long.parseLong(subject);
        String agencyIdStr = claims.get("agencyId", String.class);
        Long agencyId = agencyIdStr != null && !agencyIdStr.isEmpty() ? Long.parseLong(agencyIdStr) : null;
        return new JwtClaims(userId, agencyId, email, role, false, null);
    }

    public Long parseRefreshToken(String token) {
        String sub = getRefreshTokenSubject(token);
        if (sub != null && sub.startsWith("refresh:")) {
            return Long.parseLong(sub.substring(8));
        }
        return null;
    }

    /** Returns refresh token subject; for admin tokens it is "refresh:admin:123". */
    public String getRefreshTokenSubject(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public Long parseRefreshTokenAdminId(String token) {
        String sub = getRefreshTokenSubject(token);
        if (sub != null && sub.startsWith("refresh:admin:")) {
            return Long.parseLong(sub.substring(14));
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public record JwtClaims(Long userId, Long agencyId, String email, UserRole role, boolean admin, Long adminId) {
        public JwtClaims(Long userId, Long agencyId, String email, UserRole role) {
            this(userId, agencyId, email, role, false, null);
        }
    }
}
