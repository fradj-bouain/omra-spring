package com.omra.platform.controller;

import com.omra.platform.dto.ReferralCampaignCreateRequest;
import com.omra.platform.dto.ReferralCampaignDashboardDto;
import com.omra.platform.dto.ReferralCampaignResponseDto;
import com.omra.platform.service.ReferralCampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/referral-campaigns")
@RequiredArgsConstructor
@Tag(name = "Referral campaigns", description = "Jeux de parrainage à fenêtre temporelle")
public class ReferralCampaignController {

    private final ReferralCampaignService referralCampaignService;

    @GetMapping("/dashboard")
    @Operation(summary = "État du jeu actif, gagnants et phase")
    public ResponseEntity<ReferralCampaignDashboardDto> dashboard() {
        return ResponseEntity.ok(referralCampaignService.getDashboard());
    }

    @GetMapping
    @Operation(summary = "Historique des campagnes de l'agence")
    public ResponseEntity<List<ReferralCampaignResponseDto>> list() {
        return ResponseEntity.ok(referralCampaignService.listForAgency());
    }

    @PostMapping
    @Operation(summary = "Créer une campagne (brouillon)")
    public ResponseEntity<ReferralCampaignResponseDto> create(@Valid @RequestBody ReferralCampaignCreateRequest body) {
        return ResponseEntity.ok(referralCampaignService.create(body));
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activer la campagne (ferme toute autre campagne ACTIVE)")
    public ResponseEntity<ReferralCampaignResponseDto> activate(@PathVariable Long id) {
        return ResponseEntity.ok(referralCampaignService.activate(id));
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Fermer manuellement la campagne active")
    public ResponseEntity<ReferralCampaignResponseDto> close(@PathVariable Long id) {
        return ResponseEntity.ok(referralCampaignService.closeManual(id));
    }
}
