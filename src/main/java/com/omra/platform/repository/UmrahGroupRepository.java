package com.omra.platform.repository;

import com.omra.platform.entity.UmrahGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UmrahGroupRepository extends JpaRepository<UmrahGroup, Long> {

    Page<UmrahGroup> findByAgencyIdAndDeletedAtIsNull(Long agencyId, Pageable pageable);

    Page<UmrahGroup> findByDeletedAtIsNull(Pageable pageable);
}
