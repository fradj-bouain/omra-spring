package com.omra.platform.service;

import com.omra.platform.dto.AdminDto;
import com.omra.platform.dto.AuthRequest;
import com.omra.platform.dto.AuthResponse;
import com.omra.platform.entity.Admin;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.repository.AdminRepository;
import com.omra.platform.repository.RefreshTokenRepository;
import com.omra.platform.security.JwtProperties;
import com.omra.platform.security.JwtService;
import com.omra.platform.security.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;

    @Transactional
    public AuthResponse login(AuthRequest request) {
        Admin admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        if (Boolean.FALSE.equals(admin.getActive())) {
            throw new ForbiddenException("Admin account is disabled");
        }

        refreshTokenRepository.deleteByAdminId(admin.getId());
        String accessToken = jwtService.generateAccessTokenForAdmin(admin.getId(), admin.getEmail());
        String refreshToken = createRefreshTokenForAdmin(admin.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .admin(toDto(admin))
                .build();
    }

    private String createRefreshTokenForAdmin(Long adminId) {
        String tokenValue = jwtService.generateRefreshTokenForAdmin(adminId);
        RefreshToken token = RefreshToken.builder()
                .userId(null)
                .adminId(adminId)
                .token(tokenValue)
                .expiryDate(Instant.now().plusSeconds(jwtProperties.getRefreshTokenValidity()))
                .createdAt(Instant.now())
                .build();
        try {
            refreshTokenRepository.save(token);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage() != null && e.getMessage().contains("user_id")) {
                throw new BadRequestException(
                    "Database schema update required. Run this SQL on your database then restart: ALTER TABLE refresh_tokens ALTER COLUMN user_id DROP NOT NULL;");
            }
            throw e;
        }
        return tokenValue;
    }

    @Transactional
    public AuthResponse refresh(String token) {
        if (token == null || token.isBlank()) {
            throw new BadRequestException("Refresh token required");
        }
        RefreshToken stored = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Refresh token not found or expired"));

        if (stored.getAdminId() == null) {
            throw new BadRequestException("Not an admin refresh token");
        }
        if (stored.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(stored);
            throw new BadRequestException("Refresh token expired");
        }

        Admin admin = adminRepository.findById(stored.getAdminId())
                .orElseThrow(() -> new BadRequestException("Admin not found"));
        if (Boolean.FALSE.equals(admin.getActive())) {
            throw new ForbiddenException("Admin account is disabled");
        }

        String accessToken = jwtService.generateAccessTokenForAdmin(admin.getId(), admin.getEmail());
        String newRefreshToken = createRefreshTokenForAdmin(admin.getId());
        refreshTokenRepository.delete(stored);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .admin(toDto(admin))
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenRepository.findByToken(refreshToken).ifPresent(refreshTokenRepository::delete);
        }
    }

    private static AdminDto toDto(Admin a) {
        return AdminDto.builder()
                .id(a.getId())
                .username(a.getUsername())
                .email(a.getEmail())
                .telephone(a.getTelephone())
                .cin(a.getCin())
                .active(a.getActive())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
