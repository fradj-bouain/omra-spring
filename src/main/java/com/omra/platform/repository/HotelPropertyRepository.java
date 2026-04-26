package com.omra.platform.repository;

import com.omra.platform.entity.HotelProperty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HotelPropertyRepository extends JpaRepository<HotelProperty, Long> {

    List<HotelProperty> findByAgencyIdOrderByNameAsc(Long agencyId);

    Optional<HotelProperty> findByIdAndAgencyId(Long id, Long agencyId);
}
