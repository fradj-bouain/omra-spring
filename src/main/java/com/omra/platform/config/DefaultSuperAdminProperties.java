package com.omra.platform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.default-super-admin")
public class DefaultSuperAdminProperties {

    private boolean enabled = true;
    private String email = "superadmin@omra.local";
    private String password = "SuperAdmin123!";
}
