package com.omra.platform.repository;

import com.omra.platform.entity.Agency;
import com.omra.platform.entity.enums.AgencyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgencyRepository extends JpaRepository<Agency, Long> {

    long countByStatus(AgencyStatus status);
}
