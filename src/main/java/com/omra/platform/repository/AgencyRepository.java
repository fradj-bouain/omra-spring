package com.omra.platform.repository;

import com.omra.platform.entity.Agency;
import com.omra.platform.entity.enums.AgencyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgencyRepository extends JpaRepository<Agency, Long> {

    long countByStatus(AgencyStatus status);

    List<Agency> findByParentAgencyId(Long parentAgencyId);

    long countByParentAgencyIdAndStatus(Long parentAgencyId, AgencyStatus status);
}
