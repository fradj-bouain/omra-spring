package com.omra.platform.controller;

import com.omra.platform.dto.AgencySubscriptionDto;
import com.omra.platform.dto.AgencySubscriptionSummaryDto;
import com.omra.platform.dto.AssignAgencySubscriptionRequest;
import com.omra.platform.dto.PageResponse;
import com.omra.platform.dto.UpdateAgencySubscriptionRequest;
import com.omra.platform.service.AgencySubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agencies/{agencyId}/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Agency subscriptions", description = "Abonnements par agence (super-admin)")
public class AgencySubscriptionController {

    private final AgencySubscriptionService agencySubscriptionService;

    @GetMapping
    @Operation(summary = "Historique paginé")
    public ResponseEntity<PageResponse<AgencySubscriptionDto>> list(
            @PathVariable Long agencyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(agencySubscriptionService.listByAgency(agencyId, page, size));
    }

    @GetMapping("/summary")
    @Operation(summary = "Dernier abonnement + couverture valide actuelle")
    public ResponseEntity<AgencySubscriptionSummaryDto> summary(@PathVariable Long agencyId) {
        return ResponseEntity.ok(AgencySubscriptionSummaryDto.builder()
                .latest(agencySubscriptionService.getLatest(agencyId))
                .currentValid(agencySubscriptionService.getCurrentValid(agencyId))
                .build());
    }

    @PostMapping
    @Operation(summary = "Affecter un nouvel abonnement (les précédents actifs / en attente sont annulés)")
    public ResponseEntity<AgencySubscriptionDto> assign(
            @PathVariable Long agencyId, @Valid @RequestBody AssignAgencySubscriptionRequest request) {
        return ResponseEntity.ok(agencySubscriptionService.assign(agencyId, request));
    }

    @PutMapping("/{subscriptionId}")
    @Operation(summary = "Modifier période, paiement ou statut")
    public ResponseEntity<AgencySubscriptionDto> update(
            @PathVariable Long agencyId,
            @PathVariable Long subscriptionId,
            @RequestBody UpdateAgencySubscriptionRequest request) {
        return ResponseEntity.ok(agencySubscriptionService.update(agencyId, subscriptionId, request));
    }

    @PostMapping("/refresh-agency-state")
    @Operation(summary = "Recalculer statut agence depuis les abonnements (après correction manuelle en base)")
    public ResponseEntity<Void> refreshState(@PathVariable Long agencyId) {
        agencySubscriptionService.refreshAgencyAccessState(agencyId);
        return ResponseEntity.noContent().build();
    }
}
