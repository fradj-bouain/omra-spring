package com.omra.platform.repository;

import com.omra.platform.entity.Pilgrim;
import com.omra.platform.entity.enums.VisaStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PilgrimRepository extends JpaRepository<Pilgrim, Long> {

    Optional<Pilgrim> findByIdAndAgencyIdAndDeletedAtIsNull(Long id, Long agencyId);

    Page<Pilgrim> findByAgencyIdAndDeletedAtIsNull(Long agencyId, Pageable pageable);

    Page<Pilgrim> findByDeletedAtIsNull(Pageable pageable);

    long countByDeletedAtIsNull();

    long countByAgencyIdAndDeletedAtIsNull(Long agencyId);

    long countByDeletedAtIsNullAndVisaStatusIn(Collection<VisaStatus> statuses);

    boolean existsByAgencyIdAndPassportNumberAndDeletedAtIsNull(Long agencyId, String passportNumber);

    boolean existsByAgencyIdAndPassportNumberAndDeletedAtIsNullAndIdNot(Long agencyId, String passportNumber, Long excludeId);

    @Query("SELECT p FROM Pilgrim p WHERE p.agencyId = :agencyId AND p.deletedAt IS NULL AND "
            + "(LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "(p.passportNumber IS NOT NULL AND LOWER(p.passportNumber) LIKE LOWER(CONCAT('%', :q, '%')))) "
            + "ORDER BY p.lastName ASC, p.firstName ASC")
    List<Pilgrim> searchForAutocomplete(@Param("agencyId") Long agencyId, @Param("q") String q, Pageable pageable);
}
