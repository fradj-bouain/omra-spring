package com.omra.platform.repository;

import com.omra.platform.entity.HotelOfferReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HotelOfferReservationRepository extends JpaRepository<HotelOfferReservation, Long> {

    List<HotelOfferReservation> findByHotelAgencyIdOrderByCreatedAtDesc(Long hotelAgencyId);

    List<HotelOfferReservation> findByTravelAgencyIdOrderByCreatedAtDesc(Long travelAgencyId);

    Optional<HotelOfferReservation> findByIdAndHotelAgencyId(Long id, Long hotelAgencyId);
}

