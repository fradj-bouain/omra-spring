package com.omra.platform.repository;

import com.omra.platform.entity.TransportOfferReservation;
import com.omra.platform.entity.enums.HotelReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransportOfferReservationRepository extends JpaRepository<TransportOfferReservation, Long> {

    long countByTransportAgencyId(Long transportAgencyId);

    long countByTransportAgencyIdAndStatus(Long transportAgencyId, HotelReservationStatus status);

    List<TransportOfferReservation> findTop5ByTransportAgencyIdOrderByCreatedAtDesc(Long transportAgencyId);

    List<TransportOfferReservation> findByTransportAgencyIdOrderByCreatedAtDesc(Long transportAgencyId);

    List<TransportOfferReservation> findByTravelAgencyIdOrderByCreatedAtDesc(Long travelAgencyId);

    Optional<TransportOfferReservation> findByIdAndTransportAgencyId(Long id, Long transportAgencyId);
}
