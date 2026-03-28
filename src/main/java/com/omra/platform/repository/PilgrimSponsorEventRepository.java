package com.omra.platform.repository;

import com.omra.platform.entity.PilgrimSponsorEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PilgrimSponsorEventRepository extends JpaRepository<PilgrimSponsorEvent, Long> {

    boolean existsByReferredPilgrimId(Long referredPilgrimId);
}
