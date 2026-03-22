package com.omra.platform.repository;

import com.omra.platform.entity.Referral;
import com.omra.platform.entity.enums.ReferralStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReferralRepository extends JpaRepository<Referral, Long> {

    List<Referral> findByReferrerIdOrderByCreatedAtDesc(Long referrerId);

    Optional<Referral> findByReferredId(Long referredId);

    boolean existsByReferrerIdAndReferredId(Long referrerId, Long referredId);

    long countByReferrerId(Long referrerId);

    long countByReferrerIdAndStatus(Long referrerId, ReferralStatus status);

    long countByReferrerIdAndRewardGivenTrue(Long referrerId);
}
