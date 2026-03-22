package com.omra.platform.dto.mobile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.omra.platform.dto.AgencyDto;
import com.omra.platform.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileAccompagnateurLoginDataDto {

    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private UserDto user;
    private AgencyDto agency;
}
