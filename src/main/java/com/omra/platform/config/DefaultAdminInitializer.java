package com.omra.platform.config;

import com.omra.platform.entity.Admin;
import com.omra.platform.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates default platform Admin at startup (e.g. superadmin@omra.local).
 * Admin can create agencies and activate/deactivate agency accounts.
 */
@Component
@Order(2)
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.default-admin.enabled", havingValue = "true")
public class DefaultAdminInitializer implements ApplicationRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final DefaultAdminProperties properties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String email = properties.getEmail();
        if (adminRepository.findByEmail(email).isPresent()) {
            log.debug("Default admin already exists: {}", email);
            return;
        }
        Admin admin = Admin.builder()
                .username(properties.getUsername())
                .email(email)
                .password(passwordEncoder.encode(properties.getPassword()))
                .telephone(properties.getTelephone() != null && !properties.getTelephone().isBlank() ? properties.getTelephone() : null)
                .cin(properties.getCin() != null && !properties.getCin().isBlank() ? properties.getCin() : null)
                .active(true)
                .build();
        adminRepository.save(admin);
        log.info("Default platform admin created: {} (change password in production)", email);
    }
}
