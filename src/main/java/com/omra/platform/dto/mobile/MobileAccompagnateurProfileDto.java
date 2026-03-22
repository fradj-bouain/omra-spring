package com.omra.platform.dto.mobile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.omra.platform.dto.AgencyDto;
import com.omra.platform.entity.enums.UserRole;
import com.omra.platform.entity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Profil accompagnateur pour l'app mobile (GET /me, GET /profile).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileAccompagnateurProfileDto {

    private Long id;
    private Long agencyId;
    private String name;
    private String email;
    private String phone;
    private UserRole role;
    private UserStatus status;
    private String avatar;
    private Instant lastLogin;
    private Instant createdAt;
    /** Agence (résumé) */
    private AgencyDto agency;
}
