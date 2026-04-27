package com.omra.platform.controller;

import com.omra.platform.dto.*;
import com.omra.platform.service.HotelOperatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotel-operator")
@RequiredArgsConstructor
@Tag(name = "Hotel operator", description = "Hotels and offers for HOTEL-kind agencies")
public class HotelOperatorController {

    private final HotelOperatorService hotelOperatorService;

    @GetMapping("/properties")
    @Operation(summary = "List hotel properties for the tenant")
    public ResponseEntity<List<HotelPropertyDto>> listProperties() {
        return ResponseEntity.ok(hotelOperatorService.listProperties());
    }

    @PostMapping("/properties")
    @Operation(summary = "Create a hotel property")
    public ResponseEntity<HotelPropertyDto> createProperty(@RequestBody HotelPropertyWriteDto body) {
        return ResponseEntity.ok(hotelOperatorService.createProperty(body));
    }

    @PutMapping("/properties/{id}")
    @Operation(summary = "Update a hotel property")
    public ResponseEntity<HotelPropertyDto> updateProperty(
            @PathVariable Long id, @RequestBody HotelPropertyWriteDto body) {
        return ResponseEntity.ok(hotelOperatorService.updateProperty(id, body));
    }

    @DeleteMapping("/properties/{id}")
    @Operation(summary = "Delete a hotel property (cascades offers)")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id) {
        hotelOperatorService.deleteProperty(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/offers")
    @Operation(summary = "List offers, optionally filtered by property")
    public ResponseEntity<List<HotelOfferDto>> listOffers(@RequestParam(required = false) Long propertyId) {
        return ResponseEntity.ok(hotelOperatorService.listOffers(propertyId));
    }

    @PostMapping("/offers")
    @Operation(summary = "Create an offer")
    public ResponseEntity<HotelOfferDto> createOffer(@RequestBody HotelOfferWriteDto body) {
        return ResponseEntity.ok(hotelOperatorService.createOffer(body));
    }

    @PutMapping("/offers/{id}")
    @Operation(summary = "Update an offer")
    public ResponseEntity<HotelOfferDto> updateOffer(
            @PathVariable Long id, @RequestBody HotelOfferWriteDto body) {
        return ResponseEntity.ok(hotelOperatorService.updateOffer(id, body));
    }

    @PostMapping("/offers/{id}/status")
    @Operation(summary = "Activate or disable an offer")
    public ResponseEntity<HotelOfferDto> setOfferStatus(
            @PathVariable Long id, @RequestParam boolean active) {
        return ResponseEntity.ok(hotelOperatorService.setOfferStatus(id, active));
    }

    @DeleteMapping("/offers/{id}")
    @Operation(summary = "Delete an offer")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        hotelOperatorService.deleteOffer(id);
        return ResponseEntity.noContent().build();
    }
}
