package com.omra.platform.dto;

import com.omra.platform.entity.enums.UserRole;
import com.omra.platform.entity.enums.UserStatus;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private Long id;
    private Long agencyId;
    private String name;
    private String email;
    private String phone;
    /** Used only on create; never returned in API response */
    private String password;
    private UserRole role;
    private UserStatus status;
    private String avatar;
    private Instant lastLogin;
    private Boolean emailVerified;
    private Instant createdAt;
    /** User's referral code (for sharing). Filled on GET. */
    private String referralCode;
    /** Referral code to apply at signup (create only). */
    private String referralCodeAtSignup;
}
