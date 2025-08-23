package com.integrixs.backend.controller;

import com.integrixs.shared.dto.DashboardStatsDTO;
import com.integrixs.shared.dto.RecentMessageDTO;
import com.integrixs.shared.dto.ChannelStatusDTO;
import com.integrixs.backend.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats(
            @RequestParam(required = false) String businessComponentId) {
        DashboardStatsDTO stats = dashboardService.getDashboardStats(businessComponentId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/metrics")
    public ResponseEntity<DashboardStatsDTO> getDashboardMetrics(
            @RequestParam(required = false) String businessComponentId) {
        // Reusing stats endpoint for metrics as frontend transforms them
        DashboardStatsDTO stats = dashboardService.getDashboardStats(businessComponentId);
        return ResponseEntity.ok(stats);
    }
}