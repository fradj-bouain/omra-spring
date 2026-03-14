package com.omra.platform.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private UserDto user;
    private AgencyDto agency;
    /** Set when logged in as platform Admin (superadmin). */
    private AdminDto admin;
}
