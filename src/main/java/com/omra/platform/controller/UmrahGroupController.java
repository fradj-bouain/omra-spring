package com.omra.platform.controller;

import com.omra.platform.dto.BusDto;
import com.omra.platform.dto.FlightDto;
import com.omra.platform.dto.PageResponse;
import com.omra.platform.dto.PilgrimDto;
import com.omra.platform.dto.UmrahGroupDto;
import com.omra.platform.service.BusService;
import com.omra.platform.service.FlightService;
import com.omra.platform.service.UmrahGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Umrah Groups", description = "Group management APIs")
public class UmrahGroupController {

    private final UmrahGroupService umrahGroupService;
    private final FlightService flightService;
    private final BusService busService;

    @GetMapping
    @Operation(summary = "Get groups (paginated)")
    public ResponseEntity<PageResponse<UmrahGroupDto>> getGroups(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(umrahGroupService.getGroups(PageRequest.of(page - 1, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get group by ID")
    public ResponseEntity<UmrahGroupDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(umrahGroupService.getById(id));
    }

    @GetMapping("/{id}/pilgrims")
    @Operation(summary = "Get pilgrims in group")
    public ResponseEntity<List<PilgrimDto>> getGroupPilgrims(@PathVariable Long id) {
        return ResponseEntity.ok(umrahGroupService.getGroupPilgrims(id));
    }

    @GetMapping("/{id}/flights")
    @Operation(summary = "Get flights linked to this group")
    public ResponseEntity<List<FlightDto>> getGroupFlights(@PathVariable Long id) {
        return ResponseEntity.ok(flightService.getByGroupId(id));
    }

    @GetMapping("/{id}/buses")
    @Operation(summary = "Get buses linked to this group")
    public ResponseEntity<List<BusDto>> getGroupBuses(@PathVariable Long id) {
        return ResponseEntity.ok(busService.getBusesByGroup(id));
    }

    @PostMapping
    @Operation(summary = "Create group")
    public ResponseEntity<UmrahGroupDto> create(@RequestBody UmrahGroupDto dto) {
        return ResponseEntity.ok(umrahGroupService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update group")
    public ResponseEntity<UmrahGroupDto> update(@PathVariable Long id, @RequestBody UmrahGroupDto dto) {
        return ResponseEntity.ok(umrahGroupService.update(id, dto));
    }

    @PostMapping("/{id}/pilgrims")
    @Operation(summary = "Add pilgrim to group")
    public ResponseEntity<Void> addPilgrim(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long pilgrimId = body.get("pilgrimId");
        if (pilgrimId == null) pilgrimId = body.get("pilgrim_id");
        if (pilgrimId == null) throw new IllegalArgumentException("pilgrimId required");
        umrahGroupService.addPilgrimToGroup(id, pilgrimId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/pilgrims/{pilgrimId}")
    @Operation(summary = "Remove pilgrim from group")
    public ResponseEntity<Void> removePilgrim(@PathVariable Long id, @PathVariable Long pilgrimId) {
        umrahGroupService.removePilgrimFromGroup(id, pilgrimId);
        return ResponseEntity.noContent().build();
    }
}
