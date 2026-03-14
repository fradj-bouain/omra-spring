package com.omra.platform.controller;

import com.omra.platform.dto.GroupHotelDto;
import com.omra.platform.dto.HotelDto;
import com.omra.platform.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
@Tag(name = "Hotels", description = "Hotel management APIs")
public class HotelController {

    private final HotelService hotelService;

    @GetMapping
    @Operation(summary = "Get all hotels")
    public ResponseEntity<List<HotelDto>> getAll() {
        return ResponseEntity.ok(hotelService.getAllHotels());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get hotel by ID")
    public ResponseEntity<HotelDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.getHotelById(id));
    }

    @PostMapping
    @Operation(summary = "Create hotel")
    public ResponseEntity<HotelDto> create(@RequestBody HotelDto dto) {
        return ResponseEntity.ok(hotelService.createHotel(dto));
    }

    @GetMapping("/groups/{groupId}")
    @Operation(summary = "Get hotels assigned to group")
    public ResponseEntity<List<GroupHotelDto>> getByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(hotelService.getHotelsByGroup(groupId));
    }

    @PostMapping("/groups")
    @Operation(summary = "Assign hotel to group")
    public ResponseEntity<GroupHotelDto> assignToGroup(@RequestBody GroupHotelDto dto) {
        return ResponseEntity.ok(hotelService.assignHotelToGroup(dto));
    }
}
