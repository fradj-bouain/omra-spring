package com.omra.platform.controller;

import com.omra.platform.dto.AgencyDto;
import com.omra.platform.dto.AgencyMetricsDto;
import com.omra.platform.dto.AgencyThemeDto;
import com.omra.platform.dto.PageResponse;
import com.omra.platform.dto.SubAgencyQuotaDto;
import com.omra.platform.service.AgencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? 20 : Math.min(size, 200);
        return ResponseEntity.ok(agencyService.getAgencies(PageRequest.of(safePage, safeSize)));
    }

    @GetMapping("/theme")
    @Operation(summary = "Get current agency theme")
    public ResponseEntity<AgencyThemeDto> getTheme() {
        return ResponseEntity.ok(agencyService.getTheme());
    }

    @GetMapping("/{id}/theme")
    @Operation(summary = "Get agency theme by id (same tenant or super admin)")
    public ResponseEntity<AgencyThemeDto> getThemeByAgencyId(@PathVariable Long id) {
        return ResponseEntity.ok(agencyService.getThemeForAgency(id));
    }

    @GetMapping("/{id}/metrics")
    @Operation(summary = "Indicateurs agence (utilisateurs, pèlerins, groupes, encaissements)")
    public ResponseEntity<AgencyMetricsDto> getMetrics(@PathVariable Long id) {
        return ResponseEntity.ok(agencyService.getMetrics(id));
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

    @GetMapping("/{parentId}/subs")
    @Operation(summary = "List direct sub-agencies of a main agency")
    public ResponseEntity<List<AgencyDto>> listSubAgencies(@PathVariable Long parentId) {
        return ResponseEntity.ok(agencyService.listSubAgencies(parentId));
    }

    @PostMapping("/{parentId}/subs")
    @Operation(summary = "Create a sub-agency under a main agency (agency admin or super admin)")
    public ResponseEntity<AgencyDto> createSubAgency(@PathVariable Long parentId, @RequestBody AgencyDto dto) {
        return ResponseEntity.ok(agencyService.createSubAgency(parentId, dto));
    }

    @GetMapping("/{parentId}/sub-agency-quota")
    @Operation(summary = "Active sub-agency count vs subscription plan limit")
    public ResponseEntity<SubAgencyQuotaDto> getSubAgencyQuota(@PathVariable Long parentId) {
        return ResponseEntity.ok(agencyService.getSubAgencyQuota(parentId));
    }

    @PostMapping("/{parentId}/subs/{subId}/deactivate")
    @Operation(summary = "Deactivate a sub-agency (main agency admin)")
    public ResponseEntity<AgencyDto> deactivateSubAgency(@PathVariable Long parentId, @PathVariable Long subId) {
        return ResponseEntity.ok(agencyService.deactivateSubAgency(parentId, subId));
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
