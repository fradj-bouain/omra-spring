package com.omra.platform.repository;

import com.omra.platform.entity.User;
import com.omra.platform.entity.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    boolean existsByEmail(String email);

    Page<User> findByAgencyIdAndDeletedAtIsNull(Long agencyId, Pageable pageable);

    Page<User> findByAgencyIdIsNullAndDeletedAtIsNull(Pageable pageable);

    java.util.List<User> findByPilgrimIdAndDeletedAtIsNull(Long pilgrimId);
}
