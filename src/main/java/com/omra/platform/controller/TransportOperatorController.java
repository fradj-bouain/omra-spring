package com.omra.platform.controller;

import com.omra.platform.dto.*;
import com.omra.platform.entity.enums.HotelReservationStatus;
import com.omra.platform.service.TransportOperatorService;
import com.omra.platform.service.TransportReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transport-operator")
@RequiredArgsConstructor
@Tag(name = "Transport operator", description = "Fleet and offers for TRANSPORT-kind agencies")
public class TransportOperatorController {

    private final TransportOperatorService transportOperatorService;
    private final TransportReservationService transportReservationService;

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard transporteur : volumes et dernières demandes")
    public ResponseEntity<TransportDashboardDto> getDashboard() {
        return ResponseEntity.ok(transportOperatorService.getDashboard());
    }

    @GetMapping("/vehicles")
    @Operation(summary = "Liste des véhicules du tenant")
    public ResponseEntity<List<TransportVehicleDto>> listVehicles() {
        return ResponseEntity.ok(transportOperatorService.listVehicles());
    }

    @PostMapping("/vehicles")
    @Operation(summary = "Créer un véhicule")
    public ResponseEntity<TransportVehicleDto> createVehicle(@RequestBody TransportVehicleWriteDto body) {
        return ResponseEntity.ok(transportOperatorService.createVehicle(body));
    }

    @PutMapping("/vehicles/{id}")
    @Operation(summary = "Mettre à jour un véhicule")
    public ResponseEntity<TransportVehicleDto> updateVehicle(
            @PathVariable Long id, @RequestBody TransportVehicleWriteDto body) {
        return ResponseEntity.ok(transportOperatorService.updateVehicle(id, body));
    }

    @DeleteMapping("/vehicles/{id}")
    @Operation(summary = "Masquer un véhicule (suppression logique ; masque aussi ses offres)")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        transportOperatorService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/offers")
    @Operation(summary = "Liste des offres, filtre optionnel par véhicule")
    public ResponseEntity<List<TransportOfferDto>> listOffers(@RequestParam(required = false) Long vehicleId) {
        return ResponseEntity.ok(transportOperatorService.listOffers(vehicleId));
    }

    @PostMapping("/offers")
    @Operation(summary = "Créer une offre")
    public ResponseEntity<TransportOfferDto> createOffer(@RequestBody TransportOfferWriteDto body) {
        return ResponseEntity.ok(transportOperatorService.createOffer(body));
    }

    @PutMapping("/offers/{id}")
    @Operation(summary = "Mettre à jour une offre")
    public ResponseEntity<TransportOfferDto> updateOffer(
            @PathVariable Long id, @RequestBody TransportOfferWriteDto body) {
        return ResponseEntity.ok(transportOperatorService.updateOffer(id, body));
    }

    @PostMapping("/offers/{id}/status")
    @Operation(summary = "Activer ou désactiver une offre")
    public ResponseEntity<TransportOfferDto> setOfferStatus(
            @PathVariable Long id, @RequestParam boolean active) {
        return ResponseEntity.ok(transportOperatorService.setOfferStatus(id, active));
    }

    @DeleteMapping("/offers/{id}")
    @Operation(summary = "Masquer une offre (suppression logique)")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        transportOperatorService.deleteOffer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservations")
    @Operation(summary = "Demandes de réservation entrantes (TRANSPORT)")
    public ResponseEntity<List<TransportReservationDto>> listReservations() {
        return ResponseEntity.ok(transportReservationService.listIncomingReservations());
    }

    @PostMapping("/reservations/{id}/status")
    @Operation(summary = "Mettre à jour le statut d'une réservation")
    public ResponseEntity<TransportReservationDto> setReservationStatus(
            @PathVariable Long id,
            @RequestParam HotelReservationStatus status) {
        return ResponseEntity.ok(transportReservationService.setReservationStatus(id, status));
    }
}
