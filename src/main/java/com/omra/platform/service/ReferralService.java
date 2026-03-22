package com.omra.platform.service;

import com.omra.platform.dto.ReferralDto;
import com.omra.platform.dto.ReferralStatsDto;
import com.omra.platform.entity.Referral;
import com.omra.platform.entity.User;
import com.omra.platform.entity.enums.ReferralStatus;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.ReferralRepository;
import com.omra.platform.repository.UserRepository;
import com.omra.platform.util.ReferralCodeGenerator;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReferralService {

    private final UserRepository userRepository;
    private final ReferralRepository referralRepository;

    @Value("${app.referral.base-url:https://myapp.com}")
    private String referralBaseUrl;

    private static final int MAX_CODE_GENERATION_ATTEMPTS = 10;

    /**
     * Ensures the user has a referral code (generates one if missing). Call after user creation or on first access.
     */
    @Transactional
    public String ensureReferralCode(User user) {
        if (user.getReferralCode() != null && !user.getReferralCode().isBlank()) {
            return user.getReferralCode();
        }
        for (int i = 0; i < MAX_CODE_GENERATION_ATTEMPTS; i++) {
            String code = ReferralCodeGenerator.generate();
            if (!userRepository.existsByReferralCode(code)) {
                user.setReferralCode(code);
                userRepository.save(user);
                return code;
            }
        }
        throw new BadRequestException("Impossible de générer un code de parrainage unique");
    }

    /**
     * Get current user's referral code (generates if missing).
     */
    @Transactional
    public String getMyReferralCode() {
        Long userId = TenantContext.getUserId();
        if (userId == null) throw new ForbiddenException("Authentification requise");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (user.getDeletedAt() != null) throw new ResourceNotFoundException("User", userId);
        return ensureReferralCode(user);
    }

    /**
     * Get referral stats for current user.
     */
    @Transactional
    public ReferralStatsDto getMyStats() {
        Long userId = TenantContext.getUserId();
        if (userId == null) throw new ForbiddenException("Authentification requise");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (user.getDeletedAt() != null) throw new ResourceNotFoundException("User", userId);
        String code = ensureReferralCode(user);

        long total = referralRepository.countByReferrerId(userId);
        long pending = referralRepository.countByReferrerIdAndStatus(userId, ReferralStatus.PENDING);
        long completed = referralRepository.countByReferrerIdAndStatus(userId, ReferralStatus.COMPLETED);
        long rewardsGranted = referralRepository.countByReferrerIdAndRewardGivenTrue(userId);

        String link = referralBaseUrl + "/register?ref=" + code;
        if (!referralBaseUrl.startsWith("http")) {
            link = "https://" + referralBaseUrl.trim().replaceFirst("^https?://", "") + "/register?ref=" + code;
        }

        return ReferralStatsDto.builder()
                .referralCode(code)
                .referralLink(link)
                .totalReferrals(total)
                .pendingReferrals(pending)
                .completedReferrals(completed)
                .rewardsGranted(rewardsGranted)
                .build();
    }

    /**
     * Apply referral when a new user is created (referralCode was provided at signup).
     * Prevents self-referral and duplicate. Call from UserService after user save.
     */
    @Transactional
    public void applyReferral(Long newUserId, String referralCode) {
        if (referralCode == null || referralCode.isBlank()) return;
        String code = referralCode.trim().toUpperCase();

        User referred = userRepository.findById(newUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", newUserId));
        User referrer = userRepository.findByReferralCodeAndDeletedAtIsNull(code)
                .orElseThrow(() -> new BadRequestException("Code de parrainage invalide"));

        if (referrer.getId().equals(referred.getId())) {
            throw new BadRequestException("Auto-parrainage non autorisé");
        }
        if (referralRepository.findByReferredId(newUserId).isPresent()) {
            return; // already referred (idempotent)
        }
        if (!referrer.getAgencyId().equals(referred.getAgencyId())) {
            throw new BadRequestException("Le parrain doit appartenir à la même agence");
        }

        referred.setReferredById(referrer.getId());
        userRepository.save(referred);

        Referral referral = Referral.builder()
                .referrerId(referrer.getId())
                .referredId(referred.getId())
                .status(ReferralStatus.PENDING)
                .rewardGiven(false)
                .build();
        referralRepository.save(referral);
    }

    /**
     * Validate referral code (for signup form). Returns true if valid.
     */
    @Transactional(readOnly = true)
    public boolean validateCode(String referralCode) {
        if (referralCode == null || referralCode.isBlank()) return false;
        return userRepository.findByReferralCodeAndDeletedAtIsNull(referralCode.trim().toUpperCase()).isPresent();
    }

    /**
     * Grant reward for a referral (e.g. when referred user completes first order or email verified).
     */
    @Transactional
    public ReferralDto grantReward(Long referralId) {
        Referral referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new ResourceNotFoundException("Referral", referralId));
        if (referral.getRewardGiven()) {
            return toDto(referral);
        }
        referral.setRewardGiven(true);
        referral.setRewardGrantedAt(Instant.now());
        referral.setStatus(ReferralStatus.COMPLETED);
        referralRepository.save(referral);
        // TODO: credit referrer (points, wallet, etc.) and optionally referred user
        return toDto(referral);
    }

    /**
     * List referrals for current user (as referrer).
     */
    @Transactional(readOnly = true)
    public List<ReferralDto> getMyReferrals() {
        Long userId = TenantContext.getUserId();
        if (userId == null) throw new ForbiddenException("Authentification requise");
        return referralRepository.findByReferrerIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ReferralDto toDto(Referral r) {
        return ReferralDto.builder()
                .id(r.getId())
                .referrerId(r.getReferrerId())
                .referredId(r.getReferredId())
                .status(r.getStatus())
                .rewardGiven(r.getRewardGiven())
                .rewardGrantedAt(r.getRewardGrantedAt())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
