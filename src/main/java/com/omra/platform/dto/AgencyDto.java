package com.omra.platform.dto;

import com.omra.platform.entity.enums.AgencyStatus;
import com.omra.platform.entity.enums.ThemeMode;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencyDto {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String country;
    private String city;
    private String address;
    private String logoUrl;
    private String faviconUrl;
    private String primaryColor;
    private String secondaryColor;
    private String sidebarColor;
    private String menuColor;
    private String buttonColor;
    private String backgroundColor;
    private String cardColor;
    private String textColor;
    private ThemeMode themeMode;
    private String subscriptionPlan;
    private LocalDate subscriptionStartDate;
    private LocalDate subscriptionEndDate;
    private AgencyStatus status;
    private Instant createdAt;
}
