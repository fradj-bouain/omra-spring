package com.omra.platform.controller;

import com.omra.platform.service.PilgrimSponsorshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/pilgrim-sponsorship")
@RequiredArgsConstructor
@Tag(name = "Pilgrim sponsorship", description = "Configuration du parrainage entre pèlerins")
public class PilgrimSponsorshipController {

    private final PilgrimSponsorshipService pilgrimSponsorshipService;

    @GetMapping("/config")
    @Operation(summary = "Points attribués par filleul pèlerin (affichage agence)")
    public ResponseEntity<Map<String, Integer>> getConfig() {
        return ResponseEntity.ok(Map.of(
                "pointsPerReferral", pilgrimSponsorshipService.getPointsPerReferral()
        ));
    }
}
