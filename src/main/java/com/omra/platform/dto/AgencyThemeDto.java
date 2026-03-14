package com.omra.platform.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencyThemeDto {

    private String logoUrl;
    private String faviconUrl;
    private String primaryColor;
    private String secondaryColor;
    private String menuColor;
    private String buttonColor;
    private String backgroundColor;
    private String textColor;
}
