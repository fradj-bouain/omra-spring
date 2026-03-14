package com.omra.platform.config;

import com.omra.platform.entity.User;
import com.omra.platform.entity.enums.UserRole;
import com.omra.platform.entity.enums.UserStatus;
import com.omra.platform.repository.UserRepository;
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
 * Crée un compte super admin de test par défaut au démarrage si activé et si aucun utilisateur avec cet email n'existe.
 */
@Component
@Order(1)
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.default-super-admin.enabled", havingValue = "true")
public class DefaultSuperAdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DefaultSuperAdminProperties properties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String email = properties.getEmail();
        if (userRepository.findByEmailAndDeletedAtIsNull(email).isPresent()) {
            log.debug("Super admin déjà existant: {}", email);
            return;
        }
        User superAdmin = User.builder()
                .agencyId(null)
                .name("Super Admin")
                .email(email)
                .password(passwordEncoder.encode(properties.getPassword()))
                .role(UserRole.SUPER_ADMIN)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .build();
        userRepository.save(superAdmin);
        log.info("Compte super admin de test créé: {} (à changer en production)", email);
    }
}
