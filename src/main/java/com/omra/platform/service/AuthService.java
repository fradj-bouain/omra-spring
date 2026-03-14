package com.omra.platform.service;

import com.omra.platform.dto.*;
import com.omra.platform.entity.Agency;
import com.omra.platform.entity.User;
import com.omra.platform.entity.enums.UserStatus;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.mapper.AgencyMapper;
import com.omra.platform.mapper.UserMapper;
import com.omra.platform.repository.AgencyRepository;
import com.omra.platform.repository.RefreshTokenRepository;
import com.omra.platform.repository.UserRepository;
import com.omra.platform.security.JwtProperties;
import com.omra.platform.security.JwtService;
import com.omra.platform.security.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AgencyRepository agencyRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AgencyMapper agencyMapper;
    private final JwtProperties jwtProperties;

    @Transactional
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        if (user.getStatus() == UserStatus.DISABLED) {
            throw new ForbiddenException("Account is disabled");
        }

        user.setLastLogin(Instant.now());
        userRepository.save(user);

        revokeRefreshTokensByUser(user.getId());
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getAgencyId(), user.getEmail(), user.getRole());
        String refreshToken = createRefreshToken(user.getId());

        Agency agency = null;
        if (user.getAgencyId() != null) {
            agency = agencyRepository.findById(user.getAgencyId()).orElse(null);
        }

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userMapper.toDto(user))
                .agency(agency != null ? agencyMapper.toDto(agency) : null)
                .build();
    }

    @Transactional
    public AuthResponse refresh(String token) {
        Long userId = jwtService.parseRefreshToken(token);
        if (userId == null) {
            throw new BadRequestException("Invalid refresh token");
        }

        RefreshToken stored = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Refresh token not found or expired"));

        if (stored.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(stored);
            throw new BadRequestException("Refresh token expired");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getDeletedAt() != null || user.getStatus() == UserStatus.DISABLED) {
            throw new ForbiddenException("Account is disabled");
        }

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getAgencyId(), user.getEmail(), user.getRole());
        String newRefreshToken = createRefreshToken(user.getId());
        refreshTokenRepository.delete(stored);

        Agency agency = null;
        if (user.getAgencyId() != null) {
            agency = agencyRepository.findById(user.getAgencyId()).orElse(null);
        }

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .user(userMapper.toDto(user))
                .agency(agency != null ? agencyMapper.toDto(agency) : null)
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenRepository.findByToken(refreshToken).ifPresent(refreshTokenRepository::delete);
        }
    }

    private String createRefreshToken(Long userId) {
        String tokenValue = jwtService.generateRefreshToken(userId);
        RefreshToken token = RefreshToken.builder()
                .userId(userId)
                .token(tokenValue)
                .expiryDate(Instant.now().plusSeconds(jwtProperties.getRefreshTokenValidity()))
                .createdAt(Instant.now())
                .build();
        refreshTokenRepository.save(token);
        return tokenValue;
    }

    private void revokeRefreshTokensByUser(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
