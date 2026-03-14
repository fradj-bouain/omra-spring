package com.omra.platform.controller;

import com.omra.platform.dto.FlightDto;
import com.omra.platform.dto.PageResponse;
import com.omra.platform.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Tag(name = "Flights", description = "Flight management APIs")
public class FlightController {

    private final FlightService flightService;

    @GetMapping
    @Operation(summary = "Get flights (paginated)")
    public ResponseEntity<PageResponse<FlightDto>> getFlights(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(flightService.getFlights(PageRequest.of(page - 1, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get flight by ID")
    public ResponseEntity<FlightDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(flightService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create flight")
    public ResponseEntity<FlightDto> create(@RequestBody FlightDto dto) {
        return ResponseEntity.ok(flightService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update flight")
    public ResponseEntity<FlightDto> update(@PathVariable Long id, @RequestBody FlightDto dto) {
        return ResponseEntity.ok(flightService.update(id, dto));
    }
}
