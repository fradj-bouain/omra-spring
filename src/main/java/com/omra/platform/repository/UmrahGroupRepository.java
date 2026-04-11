package com.omra.platform.repository;

import com.omra.platform.entity.UmrahGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface UmrahGroupRepository extends JpaRepository<UmrahGroup, Long> {

    Page<UmrahGroup> findByAgencyIdAndDeletedAtIsNull(Long agencyId, Pageable pageable);

    Page<UmrahGroup> findByAgencyIdInAndDeletedAtIsNull(Collection<Long> agencyIds, Pageable pageable);

    Page<UmrahGroup> findByDeletedAtIsNull(Pageable pageable);

    long countByDeletedAtIsNull();

    long countByAgencyIdAndDeletedAtIsNull(Long agencyId);

    long countByAgencyIdInAndDeletedAtIsNull(Collection<Long> agencyIds);
}
