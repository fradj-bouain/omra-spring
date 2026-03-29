package com.omra.platform.controller;

import com.omra.platform.dto.AgencySubscriptionDto;
import com.omra.platform.dto.AgencySubscriptionSummaryDto;
import com.omra.platform.dto.PageResponse;
import com.omra.platform.service.AgencySubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/subscriptions")
@RequiredArgsConstructor
@Tag(name = "My agency subscription", description = "Abonnement de l’agence connectée (portail agence)")
public class MyAgencySubscriptionController {

    private final AgencySubscriptionService agencySubscriptionService;

    @GetMapping("/summary")
    @Operation(summary = "Dernier enregistrement + couverture valide aujourd’hui")
    public ResponseEntity<AgencySubscriptionSummaryDto> summary() {
        return ResponseEntity.ok(agencySubscriptionService.summaryForMyAgency());
    }

    @GetMapping
    @Operation(summary = "Historique paginé des abonnements")
    public ResponseEntity<PageResponse<AgencySubscriptionDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(agencySubscriptionService.listForMyAgency(page, size));
    }
}
