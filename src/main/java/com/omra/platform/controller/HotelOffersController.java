package com.omra.platform.controller;

import com.omra.platform.dto.HotelOfferDto;
import com.omra.platform.dto.HotelReservationCreateDto;
import com.omra.platform.dto.HotelReservationDto;
import com.omra.platform.dto.HotelReservationTravelViewDto;
import com.omra.platform.service.HotelReservationService;
import com.omra.platform.service.HotelOfferBrowseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hotel-offers")
@RequiredArgsConstructor
@Tag(name = "Hotel offers", description = "Browse active hotel offers (TRAVEL agencies)")
public class HotelOffersController {

    private final HotelOfferBrowseService hotelOfferBrowseService;
    private final HotelReservationService hotelReservationService;

    @GetMapping
    @Operation(summary = "List active hotel offers (for TRAVEL agencies)")
    public ResponseEntity<List<HotelOfferDto>> listActiveOffers() {
        return ResponseEntity.ok(hotelOfferBrowseService.listActiveOffersForTravelAgencies());
    }

    @GetMapping("/my-reservations")
    @Operation(summary = "List this travel agency’s hotel-offer reservation requests and their status")
    public ResponseEntity<List<HotelReservationTravelViewDto>> listMyReservations() {
        return ResponseEntity.ok(hotelReservationService.listReservationsForTravelAgency());
    }

    @PostMapping("/{offerId}/reservations")
    @Operation(summary = "Create a reservation request on an active offer (TRAVEL agency)")
    public ResponseEntity<HotelReservationDto> createReservation(
            @PathVariable Long offerId,
            @RequestBody HotelReservationCreateDto body) {
        return ResponseEntity.ok(hotelReservationService.createReservation(offerId, body));
    }
}

