package com.omra.platform.controller;

import com.omra.platform.dto.CreatePilgrimFamilyBatchRequestDto;
import com.omra.platform.dto.CreatePilgrimFamilyBatchResponseDto;
import com.omra.platform.dto.PageResponse;
import com.omra.platform.dto.PilgrimDto;
import com.omra.platform.dto.PilgrimRegistrationRowDto;
import com.omra.platform.dto.PilgrimSearchResultDto;
import com.omra.platform.service.PilgrimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/registrations")
    @Operation(summary = "Get pilgrim registrations (INDIVIDUAL rows + FAMILY rows)")
    public ResponseEntity<PageResponse<PilgrimRegistrationRowDto>> getRegistrations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(pilgrimService.getRegistrations(PageRequest.of(page - 1, size), q));
    }

    @GetMapping("/autocomplete")
    @Operation(summary = "Recherche pèlerins (parrainage, min. 2 caractères)")
    public ResponseEntity<List<PilgrimSearchResultDto>> autocomplete(
            @RequestParam String q,
            @RequestParam(defaultValue = "15") int limit) {
        return ResponseEntity.ok(pilgrimService.autocompletePilgrims(q, limit));
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

    @PostMapping("/family-batch")
    @Operation(summary = "Créer une famille (≥2 pèlerins) : enregistrement groupe + liens family_id")
    public ResponseEntity<CreatePilgrimFamilyBatchResponseDto> createFamilyBatch(
            @Valid @RequestBody CreatePilgrimFamilyBatchRequestDto body) {
        return ResponseEntity.ok(pilgrimService.createFamilyBatch(body));
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

    @DeleteMapping("/families/{familyId}")
    @Operation(summary = "Delete pilgrim family (soft delete all members)")
    public ResponseEntity<Void> deleteFamily(@PathVariable Long familyId) {
        pilgrimService.deleteFamily(familyId);
        return ResponseEntity.noContent().build();
    }
}
