package com.omra.platform.repository;

import com.omra.platform.entity.Marketplace;
import com.omra.platform.entity.enums.MarketplaceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarketplaceRepository extends JpaRepository<Marketplace, Long> {

    List<Marketplace> findByAgencyIdOrderByNameAsc(Long agencyId);

    List<Marketplace> findByAgencyIdAndStatusOrderByNameAsc(Long agencyId, MarketplaceStatus status);
}

