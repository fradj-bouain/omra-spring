package com.omra.platform.repository;

import com.omra.platform.entity.HotelOffer;
import com.omra.platform.entity.enums.HotelOfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HotelOfferRepository extends JpaRepository<HotelOffer, Long> {

    List<HotelOffer> findByAgencyIdOrderByCreatedAtDesc(Long agencyId);

    List<HotelOffer> findByAgencyIdAndPropertyIdOrderByCreatedAtDesc(Long agencyId, Long propertyId);

    Optional<HotelOffer> findByIdAndAgencyId(Long id, Long agencyId);

    List<HotelOffer> findByStatusOrderByCreatedAtDesc(HotelOfferStatus status);
}
