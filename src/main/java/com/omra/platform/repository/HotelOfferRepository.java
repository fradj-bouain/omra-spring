package com.omra.platform.repository;

import com.omra.platform.entity.HotelOffer;
import com.omra.platform.entity.enums.HotelOfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HotelOfferRepository extends JpaRepository<HotelOffer, Long> {

    long countByAgencyIdAndDeletedAtIsNull(Long agencyId);

    long countByAgencyIdAndStatusAndDeletedAtIsNull(Long agencyId, HotelOfferStatus status);

    List<HotelOffer> findByAgencyIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long agencyId);

    List<HotelOffer> findByAgencyIdAndPropertyIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long agencyId, Long propertyId);

    Optional<HotelOffer> findByIdAndAgencyIdAndDeletedAtIsNull(Long id, Long agencyId);

    List<HotelOffer> findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(HotelOfferStatus status);

    Optional<HotelOffer> findByIdAndDeletedAtIsNull(Long id);
}
