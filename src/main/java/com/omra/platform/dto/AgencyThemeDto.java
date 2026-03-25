package com.omra.platform.dto;

import com.omra.platform.entity.enums.ThemeMode;
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
    private String sidebarColor;
    private String menuColor;
    private String buttonColor;
    private String backgroundColor;
    private String cardColor;
    private String textColor;
    private ThemeMode themeMode;
}
