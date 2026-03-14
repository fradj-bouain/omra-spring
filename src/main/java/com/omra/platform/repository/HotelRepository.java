package com.omra.platform.repository;

import com.omra.platform.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findByAgencyId(Long agencyId);
}
