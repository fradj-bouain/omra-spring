package com.omra.platform.repository;

import com.omra.platform.entity.TransportOffer;
import com.omra.platform.entity.enums.TransportOfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransportOfferRepository extends JpaRepository<TransportOffer, Long> {

    long countByAgencyIdAndDeletedAtIsNull(Long agencyId);

    long countByAgencyIdAndStatusAndDeletedAtIsNull(Long agencyId, TransportOfferStatus status);

    List<TransportOffer> findByAgencyIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long agencyId);

    List<TransportOffer> findByAgencyIdAndVehicleIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long agencyId, Long vehicleId);

    Optional<TransportOffer> findByIdAndAgencyIdAndDeletedAtIsNull(Long id, Long agencyId);

    List<TransportOffer> findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(TransportOfferStatus status);

    Optional<TransportOffer> findByIdAndDeletedAtIsNull(Long id);
}
