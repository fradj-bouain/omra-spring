package com.omra.platform.controller;

import com.omra.platform.dto.PageResponse;
import com.omra.platform.dto.PilgrimDto;
import com.omra.platform.service.PilgrimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pilgrims")
@RequiredArgsConstructor
@Tag(name = "Pilgrims", description = "Pilgrim management APIs")
public class PilgrimController {

    private final PilgrimService pilgrimService;

    @GetMapping
    @Operation(summary = "Get pilgrims (paginated)")
    public ResponseEntity<PageResponse<PilgrimDto>> getPilgrims(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(pilgrimService.getPilgrims(PageRequest.of(page - 1, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get pilgrim by ID")
    public ResponseEntity<PilgrimDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(pilgrimService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create pilgrim")
    public ResponseEntity<PilgrimDto> create(@RequestBody PilgrimDto dto) {
        return ResponseEntity.ok(pilgrimService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update pilgrim")
    public ResponseEntity<PilgrimDto> update(@PathVariable Long id, @RequestBody PilgrimDto dto) {
        return ResponseEntity.ok(pilgrimService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete pilgrim (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pilgrimService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
