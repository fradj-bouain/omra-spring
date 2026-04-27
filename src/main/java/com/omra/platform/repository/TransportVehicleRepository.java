package com.omra.platform.repository;

import com.omra.platform.entity.TransportVehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransportVehicleRepository extends JpaRepository<TransportVehicle, Long> {

    long countByAgencyIdAndDeletedAtIsNull(Long agencyId);

    List<TransportVehicle> findByAgencyIdAndDeletedAtIsNullOrderByPlateAsc(Long agencyId);

    Optional<TransportVehicle> findByIdAndAgencyIdAndDeletedAtIsNull(Long id, Long agencyId);
}
