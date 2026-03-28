package com.omra.platform.controller;

import com.omra.platform.dto.ReferralRewardTierDto;
import com.omra.platform.service.ReferralRewardTierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/referral-reward-tiers")
@RequiredArgsConstructor
@Tag(name = "Referral reward tiers", description = "Paliers de cadeaux (points) pour le parrainage pèlerins")
public class ReferralRewardTierController {

    private final ReferralRewardTierService tierService;

    @GetMapping
    @Operation(summary = "Liste des paliers de l'agence")
    public ResponseEntity<List<ReferralRewardTierDto>> list() {
        return ResponseEntity.ok(tierService.listForAgency());
    }

    @PostMapping
    @Operation(summary = "Créer un palier")
    public ResponseEntity<ReferralRewardTierDto> create(@RequestBody ReferralRewardTierDto dto) {
        return ResponseEntity.ok(tierService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un palier")
    public ResponseEntity<ReferralRewardTierDto> update(@PathVariable Long id, @RequestBody ReferralRewardTierDto dto) {
        return ResponseEntity.ok(tierService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un palier (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
