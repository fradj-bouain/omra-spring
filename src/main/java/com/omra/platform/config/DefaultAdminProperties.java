package com.omra.platform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.default-admin")
public class DefaultAdminProperties {

    private boolean enabled = true;
    private String username = "superadmin";
    private String email = "superadmin@omra.local";
    private String password = "SuperAdmin123!";
    private String telephone;
    private String cin;
}
