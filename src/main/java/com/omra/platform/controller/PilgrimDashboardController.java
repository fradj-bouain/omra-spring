package com.omra.platform.controller;

import com.omra.platform.dto.PilgrimDashboardDto;
import com.omra.platform.service.PilgrimDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pilgrim")
@RequiredArgsConstructor
@Tag(name = "Pilgrim Dashboard", description = "Pilgrim-facing dashboard API")
public class PilgrimDashboardController {

    private final PilgrimDashboardService pilgrimDashboardService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get pilgrim dashboard (group, flight, hotel, visa, documents, notifications)")
    public ResponseEntity<PilgrimDashboardDto> getDashboard() {
        return ResponseEntity.ok(pilgrimDashboardService.getDashboard());
    }
}
