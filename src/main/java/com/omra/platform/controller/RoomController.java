package com.omra.platform.controller;

import com.omra.platform.dto.GroupRoomAssignmentDto;
import com.omra.platform.dto.PageResponse;
import com.omra.platform.dto.RoomDto;
import com.omra.platform.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Rooms", description = "Hotel rooms and group room assignments")
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/hotels/{hotelId}/rooms")
    @Operation(summary = "List rooms by hotel")
    public ResponseEntity<PageResponse<RoomDto>> listByHotel(
            @PathVariable Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(roomService.getByHotel(hotelId, org.springframework.data.domain.PageRequest.of(page, size)));
    }

    @GetMapping("/rooms/{id}")
    @Operation(summary = "Get room by ID")
    public ResponseEntity<RoomDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getById(id));
    }

    @PostMapping("/rooms")
    @Operation(summary = "Create room")
    public ResponseEntity<RoomDto> create(@RequestBody RoomDto dto) {
        return ResponseEntity.ok(roomService.create(dto));
    }

    @PutMapping("/rooms/{id}")
    @Operation(summary = "Update room")
    public ResponseEntity<RoomDto> update(@PathVariable Long id, @RequestBody RoomDto dto) {
        return ResponseEntity.ok(roomService.update(id, dto));
    }

    @DeleteMapping("/rooms/{id}")
    @Operation(summary = "Delete room (soft)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/group-hotels/{groupHotelId}/room-assignments")
    @Operation(summary = "List room assignments for a group hotel")
    public ResponseEntity<List<GroupRoomAssignmentDto>> listAssignments(@PathVariable Long groupHotelId) {
        return ResponseEntity.ok(roomService.getAssignmentsByGroupHotel(groupHotelId));
    }

    @PostMapping("/group-room-assignments")
    @Operation(summary = "Assign pilgrim to room for a group hotel")
    public ResponseEntity<GroupRoomAssignmentDto> assign(@RequestBody Map<String, Long> body) {
        Long groupHotelId = body.get("groupHotelId");
        Long roomId = body.get("roomId");
        Long pilgrimId = body.get("pilgrimId");
        if (groupHotelId == null || roomId == null || pilgrimId == null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(roomService.assignPilgrimToRoom(groupHotelId, roomId, pilgrimId));
    }
}
