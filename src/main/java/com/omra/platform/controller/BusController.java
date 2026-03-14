package com.omra.platform.controller;

import com.omra.platform.dto.BusDto;
import com.omra.platform.dto.PageResponse;
import com.omra.platform.service.BusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/buses")
@RequiredArgsConstructor
@Tag(name = "Buses", description = "Buses and group bus assignments")
public class BusController {

    private final BusService busService;

    @GetMapping
    @Operation(summary = "List buses")
    public ResponseEntity<PageResponse<BusDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(busService.getBuses(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bus by ID")
    public ResponseEntity<BusDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(busService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create bus")
    public ResponseEntity<BusDto> create(@RequestBody BusDto dto) {
        return ResponseEntity.ok(busService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update bus")
    public ResponseEntity<BusDto> update(@PathVariable Long id, @RequestBody BusDto dto) {
        return ResponseEntity.ok(busService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete bus (soft)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        busService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/seats")
    @Operation(summary = "List seat numbers for bus")
    public ResponseEntity<List<String>> listSeats(@PathVariable Long id) {
        return ResponseEntity.ok(busService.getSeatNumbers(id));
    }

    @PostMapping("/assign-group")
    @Operation(summary = "Assign bus to group")
    public ResponseEntity<Void> assignToGroup(@RequestBody Map<String, Long> body) {
        Long groupId = body.get("groupId");
        Long busId = body.get("busId");
        if (groupId == null || busId == null) return ResponseEntity.badRequest().build();
        busService.assignBusToGroup(groupId, busId);
        return ResponseEntity.noContent().build();
    }
}
