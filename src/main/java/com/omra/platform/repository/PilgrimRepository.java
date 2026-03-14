package com.omra.platform.repository;

import com.omra.platform.entity.Pilgrim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PilgrimRepository extends JpaRepository<Pilgrim, Long> {

    Page<Pilgrim> findByAgencyIdAndDeletedAtIsNull(Long agencyId, Pageable pageable);

    Page<Pilgrim> findByDeletedAtIsNull(Pageable pageable);

    boolean existsByAgencyIdAndPassportNumberAndDeletedAtIsNull(Long agencyId, String passportNumber);

    boolean existsByAgencyIdAndPassportNumberAndDeletedAtIsNullAndIdNot(Long agencyId, String passportNumber, Long excludeId);
}
