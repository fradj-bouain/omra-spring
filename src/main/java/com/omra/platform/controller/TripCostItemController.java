package com.omra.platform.controller;

import com.omra.platform.dto.TripCostItemDto;
import com.omra.platform.service.TripCostItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Trip costs", description = "Trip cost breakdown per group")
public class TripCostItemController {

    private final TripCostItemService tripCostItemService;

    @GetMapping("/{groupId}/trip-costs")
    @Operation(summary = "List cost items for group")
    public ResponseEntity<List<TripCostItemDto>> list(@PathVariable Long groupId) {
        return ResponseEntity.ok(tripCostItemService.getByGroup(groupId));
    }

    @GetMapping("/{groupId}/trip-costs/items/{id}")
    @Operation(summary = "Get cost item by ID")
    public ResponseEntity<TripCostItemDto> getById(@PathVariable Long groupId, @PathVariable Long id) {
        return ResponseEntity.ok(tripCostItemService.getById(id));
    }

    @PostMapping("/{groupId}/trip-costs")
    @Operation(summary = "Create cost item")
    public ResponseEntity<TripCostItemDto> create(@PathVariable Long groupId, @RequestBody TripCostItemDto dto) {
        dto.setGroupId(groupId);
        return ResponseEntity.ok(tripCostItemService.create(dto));
    }

    @PutMapping("/{groupId}/trip-costs/items/{id}")
    @Operation(summary = "Update cost item")
    public ResponseEntity<TripCostItemDto> update(@PathVariable Long groupId, @PathVariable Long id, @RequestBody TripCostItemDto dto) {
        return ResponseEntity.ok(tripCostItemService.update(id, dto));
    }

    @DeleteMapping("/{groupId}/trip-costs/items/{id}")
    @Operation(summary = "Delete cost item (soft)")
    public ResponseEntity<Void> delete(@PathVariable Long groupId, @PathVariable Long id) {
        tripCostItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
