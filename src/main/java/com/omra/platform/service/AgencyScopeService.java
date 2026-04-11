package com.omra.platform.service;

import com.omra.platform.entity.Agency;
import com.omra.platform.repository.AgencyRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves which agency rows the current JWT may see: a sub-agency only its own id; a main agency itself plus all direct subs.
 */
@Service
@RequiredArgsConstructor
public class AgencyScopeService {

    private final AgencyRepository agencyRepository;

    public void resolveAndApply(Long agencyId) {
        if (agencyId == null) {
            TenantContext.setAccessibleAgencyIds(null);
            return;
        }
        Agency agency = agencyRepository.findById(agencyId).orElse(null);
        if (agency == null) {
            TenantContext.setAccessibleAgencyIds(List.of(agencyId));
            return;
        }
        if (agency.getParentAgencyId() == null) {
            List<Long> ids = new ArrayList<>();
            ids.add(agencyId);
            for (Agency sub : agencyRepository.findByParentAgencyId(agencyId)) {
                ids.add(sub.getId());
            }
            TenantContext.setAccessibleAgencyIds(ids);
        } else {
            TenantContext.setAccessibleAgencyIds(List.of(agencyId));
        }
    }
}
