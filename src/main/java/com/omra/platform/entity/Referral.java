package com.omra.platform.entity;

import com.omra.platform.entity.enums.ReferralStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "referrals", indexes = {
    @Index(name = "idx_referral_referrer", columnList = "referrer_id"),
    @Index(name = "idx_referral_referred", columnList = "referred_id"),
    @Index(name = "idx_referral_status", columnList = "status")
}, uniqueConstraints = {
    @UniqueConstraint(columnNames = "referred_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Referral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "referrer_id", nullable = false)
    private Long referrerId;

    @Column(name = "referred_id", nullable = false)
    private Long referredId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReferralStatus status;

    @Column(name = "reward_given", nullable = false)
    private Boolean rewardGiven;

    @Column(name = "reward_granted_at")
    private Instant rewardGrantedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referrer_id", insertable = false, updatable = false)
    private User referrer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referred_id", insertable = false, updatable = false)
    private User referred;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (rewardGiven == null) rewardGiven = false;
    }
}
