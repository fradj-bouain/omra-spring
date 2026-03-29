package com.omra.platform.controller;

import com.omra.platform.dto.SubscriptionPlanDto;
import com.omra.platform.service.SubscriptionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscription-plans")
@RequiredArgsConstructor
@Tag(name = "Subscription plans", description = "Forfaits d'abonnement (super-admin)")
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @GetMapping
    @Operation(summary = "Liste tous les forfaits")
    public ResponseEntity<List<SubscriptionPlanDto>> listAll() {
        return ResponseEntity.ok(subscriptionPlanService.listAll());
    }

    @GetMapping("/active")
    @Operation(summary = "Liste les forfaits actifs (affectation agence)")
    public ResponseEntity<List<SubscriptionPlanDto>> listActive() {
        return ResponseEntity.ok(subscriptionPlanService.listActive());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un forfait")
    public ResponseEntity<SubscriptionPlanDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionPlanService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Créer un forfait")
    public ResponseEntity<SubscriptionPlanDto> create(@Valid @RequestBody SubscriptionPlanDto dto) {
        return ResponseEntity.ok(subscriptionPlanService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un forfait")
    public ResponseEntity<SubscriptionPlanDto> update(@PathVariable Long id, @RequestBody SubscriptionPlanDto dto) {
        return ResponseEntity.ok(subscriptionPlanService.update(id, dto));
    }
}
