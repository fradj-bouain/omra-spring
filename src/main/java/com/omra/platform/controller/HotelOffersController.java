package com.omra.platform.controller;

import com.omra.platform.dto.HotelOfferDto;
import com.omra.platform.service.HotelOfferBrowseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hotel-offers")
@RequiredArgsConstructor
@Tag(name = "Hotel offers", description = "Browse active hotel offers (TRAVEL agencies)")
public class HotelOffersController {

    private final HotelOfferBrowseService hotelOfferBrowseService;

    @GetMapping
    @Operation(summary = "List active hotel offers (for TRAVEL agencies)")
    public ResponseEntity<List<HotelOfferDto>> listActiveOffers() {
        return ResponseEntity.ok(hotelOfferBrowseService.listActiveOffersForTravelAgencies());
    }
}

