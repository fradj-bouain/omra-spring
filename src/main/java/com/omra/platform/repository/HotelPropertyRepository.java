package com.omra.platform.repository;

import com.omra.platform.entity.HotelProperty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HotelPropertyRepository extends JpaRepository<HotelProperty, Long> {

    long countByAgencyIdAndDeletedAtIsNull(Long agencyId);

    List<HotelProperty> findByAgencyIdAndDeletedAtIsNullOrderByNameAsc(Long agencyId);

    Optional<HotelProperty> findByIdAndAgencyIdAndDeletedAtIsNull(Long id, Long agencyId);
}
