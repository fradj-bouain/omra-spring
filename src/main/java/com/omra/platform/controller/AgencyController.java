package com.omra.platform.controller;

import com.omra.platform.dto.AgencyDto;
import com.omra.platform.dto.AgencyThemeDto;
import com.omra.platform.dto.PageResponse;
import com.omra.platform.service.AgencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agencies")
@RequiredArgsConstructor
@Tag(name = "Agencies", description = "Agency management APIs")
public class AgencyController {

    private final AgencyService agencyService;

    @GetMapping
    @Operation(summary = "Get agencies (Super Admin)")
    public ResponseEntity<PageResponse<AgencyDto>> getAgencies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(agencyService.getAgencies(PageRequest.of(page, size)));
    }

    @GetMapping("/theme")
    @Operation(summary = "Get current agency theme")
    public ResponseEntity<AgencyThemeDto> getTheme() {
        return ResponseEntity.ok(agencyService.getTheme());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get agency by ID")
    public ResponseEntity<AgencyDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(agencyService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create agency (Super Admin)")
    public ResponseEntity<AgencyDto> create(@RequestBody AgencyDto dto) {
        return ResponseEntity.ok(agencyService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update agency")
    public ResponseEntity<AgencyDto> update(@PathVariable Long id, @RequestBody AgencyDto dto) {
        return ResponseEntity.ok(agencyService.update(id, dto));
    }

    @PutMapping("/branding")
    @Operation(summary = "Update agency branding/theme")
    public ResponseEntity<AgencyThemeDto> updateBranding(@RequestBody AgencyThemeDto dto) {
        return ResponseEntity.ok(agencyService.updateBranding(dto));
    }
}
