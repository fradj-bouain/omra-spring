package com.omra.platform.repository;

import com.omra.platform.entity.HotelOfferReservation;
import com.omra.platform.entity.enums.HotelReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HotelOfferReservationRepository extends JpaRepository<HotelOfferReservation, Long> {

    long countByHotelAgencyId(Long hotelAgencyId);

    long countByHotelAgencyIdAndStatus(Long hotelAgencyId, HotelReservationStatus status);

    List<HotelOfferReservation> findTop5ByHotelAgencyIdOrderByCreatedAtDesc(Long hotelAgencyId);

    List<HotelOfferReservation> findByHotelAgencyIdOrderByCreatedAtDesc(Long hotelAgencyId);

    List<HotelOfferReservation> findByTravelAgencyIdOrderByCreatedAtDesc(Long travelAgencyId);

    Optional<HotelOfferReservation> findByIdAndHotelAgencyId(Long id, Long hotelAgencyId);
}

