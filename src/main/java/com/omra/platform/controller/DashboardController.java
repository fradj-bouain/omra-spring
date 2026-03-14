package com.omra.platform.controller;

import com.omra.platform.dto.DashboardChartDto;
import com.omra.platform.dto.DashboardGroupKpiDto;
import com.omra.platform.dto.DashboardStatsDto;
import com.omra.platform.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Agency dashboard stats")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics")
    public ResponseEntity<DashboardStatsDto> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/group-kpis")
    @Operation(summary = "Get KPIs per group (filled capacity, total paid, price)")
    public ResponseEntity<List<DashboardGroupKpiDto>> getGroupKpis() {
        return ResponseEntity.ok(dashboardService.getGroupKpis());
    }

    @GetMapping("/chart-data")
    @Operation(summary = "Get chart data (payments over time, visa distribution)")
    public ResponseEntity<DashboardChartDto> getChartData() {
        return ResponseEntity.ok(dashboardService.getChartData());
    }
}
