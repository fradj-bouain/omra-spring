package com.omra.platform.controller;

import com.omra.platform.dto.HotelReservationCreateDto;
import com.omra.platform.dto.TransportOfferDto;
import com.omra.platform.dto.TransportReservationDto;
import com.omra.platform.dto.TransportReservationTravelViewDto;
import com.omra.platform.service.TransportOfferBrowseService;
import com.omra.platform.service.TransportReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transport-offers")
@RequiredArgsConstructor
@Tag(name = "Transport offers", description = "Browse transport rental offers (TRAVEL agencies)")
public class TransportOffersController {

    private final TransportOfferBrowseService transportOfferBrowseService;
    private final TransportReservationService transportReservationService;

    @GetMapping
    @Operation(summary = "Liste des offres transport actives (agences voyage)")
    public ResponseEntity<List<TransportOfferDto>> listActiveOffers() {
        return ResponseEntity.ok(transportOfferBrowseService.listActiveOffersForTravelAgencies());
    }

    @GetMapping("/my-reservations")
    @Operation(summary = "Réservations transport de cette agence voyage")
    public ResponseEntity<List<TransportReservationTravelViewDto>> listMyReservations() {
        return ResponseEntity.ok(transportReservationService.listReservationsForTravelAgency());
    }

    @PostMapping("/{offerId}/reservations")
    @Operation(summary = "Créer une demande de réservation sur une offre active")
    public ResponseEntity<TransportReservationDto> createReservation(
            @PathVariable Long offerId,
            @RequestBody HotelReservationCreateDto body) {
        return ResponseEntity.ok(transportReservationService.createReservation(offerId, body));
    }
}
