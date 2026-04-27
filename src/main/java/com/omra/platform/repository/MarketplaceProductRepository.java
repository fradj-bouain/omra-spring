package com.omra.platform.repository;

import com.omra.platform.entity.MarketplaceProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MarketplaceProductRepository extends JpaRepository<MarketplaceProduct, Long> {

    List<MarketplaceProduct> findByAgencyIdAndMarketplaceIdAndDeletedAtIsNullOrderByTitleAsc(Long agencyId, Long marketplaceId);

    Optional<MarketplaceProduct> findByIdAndAgencyIdAndDeletedAtIsNull(Long id, Long agencyId);
}
