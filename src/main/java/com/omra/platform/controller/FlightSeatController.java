package com.omra.platform.controller;

import com.omra.platform.dto.FlightSeatAssignmentDto;
import com.omra.platform.dto.FlightSeatDto;
import com.omra.platform.service.FlightSeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Tag(name = "Flight seats", description = "Flight seat allocation")
public class FlightSeatController {

    private final FlightSeatService flightSeatService;

    @GetMapping("/{flightId}/seats")
    @Operation(summary = "List seats for flight")
    public ResponseEntity<List<FlightSeatDto>> listSeats(@PathVariable Long flightId) {
        return ResponseEntity.ok(flightSeatService.getByFlight(flightId));
    }

    @PostMapping("/{flightId}/seats")
    @Operation(summary = "Create seats (bulk: list of seat numbers)")
    public ResponseEntity<List<FlightSeatDto>> createSeats(@PathVariable Long flightId, @RequestBody List<String> seatNumbers) {
        return ResponseEntity.ok(flightSeatService.createSeats(flightId, seatNumbers != null ? seatNumbers : List.of()));
    }

    @GetMapping("/{flightId}/seat-assignments")
    @Operation(summary = "List seat assignments for flight")
    public ResponseEntity<List<FlightSeatAssignmentDto>> listAssignments(@PathVariable Long flightId) {
        return ResponseEntity.ok(flightSeatService.getAssignmentsByFlight(flightId));
    }

    @PostMapping("/{flightId}/seat-assignments")
    @Operation(summary = "Assign pilgrim to seat")
    public ResponseEntity<FlightSeatAssignmentDto> assign(
            @PathVariable Long flightId,
            @RequestBody Map<String, Long> body) {
        Long flightSeatId = body.get("flightSeatId");
        Long pilgrimId = body.get("pilgrimId");
        if (flightSeatId == null || pilgrimId == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(flightSeatService.assignPilgrim(flightId, flightSeatId, pilgrimId));
    }
}
