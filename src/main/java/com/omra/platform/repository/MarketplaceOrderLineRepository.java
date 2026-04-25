package com.omra.platform.repository;

import com.omra.platform.entity.MarketplaceOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarketplaceOrderLineRepository extends JpaRepository<MarketplaceOrderLine, Long> {

    List<MarketplaceOrderLine> findByOrderIdOrderByIdAsc(Long orderId);
}

