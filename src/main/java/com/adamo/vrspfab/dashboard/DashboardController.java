package com.adamo.vrspfab.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "APIs for retrieving dashboard statistics")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get dashboard statistics",
               description = "Retrieves aggregated statistics for the admin dashboard, including user, vehicle, reservation, and revenue data.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved dashboard statistics"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }
}
