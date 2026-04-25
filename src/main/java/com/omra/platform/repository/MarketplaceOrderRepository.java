package com.omra.platform.repository;

import com.omra.platform.entity.MarketplaceOrder;
import com.omra.platform.entity.enums.MarketplaceOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MarketplaceOrderRepository extends JpaRepository<MarketplaceOrder, Long> {

    Page<MarketplaceOrder> findByAgencyIdOrderByCreatedAtDesc(Long agencyId, Pageable pageable);

    Page<MarketplaceOrder> findByAgencyIdAndStatusOrderByCreatedAtDesc(Long agencyId, MarketplaceOrderStatus status, Pageable pageable);

    Page<MarketplaceOrder> findByPilgrimIdOrderByCreatedAtDesc(Long pilgrimId, Pageable pageable);
}

