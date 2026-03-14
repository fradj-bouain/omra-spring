package com.omra.platform.repository;

import com.omra.platform.entity.Bus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusRepository extends JpaRepository<Bus, Long> {

    Page<Bus> findByAgencyIdAndDeletedAtIsNull(Long agencyId, Pageable pageable);
}
