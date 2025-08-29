package com.adamo.vrspfab.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "APIs for retrieving dashboard statistics and activities")
public class DashboardController {

    private final DashboardService dashboardService;
    private final ActivityService activityService;

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

    @Operation(summary = "Get dashboard analytics",
               description = "Retrieves analytics data for dashboard charts including revenue trends, vehicle utilization, and monthly performance.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved dashboard analytics"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/analytics")
    public ResponseEntity<DashboardAnalyticsDto> getDashboardAnalytics(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(dashboardService.getDashboardAnalytics(days));
    }

    @Operation(summary = "Get recent activities",
               description = "Retrieves recent activities for the dashboard activity feed.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved recent activities"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/activities")
    public ResponseEntity<Map<String, Object>> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit) {
        List<ActivityDto> activities = activityService.getRecentActivities(limit);
        return ResponseEntity.ok(Map.of("activities", activities));
    }

    @Operation(summary = "Get activity statistics",
               description = "Retrieves activity statistics for analytics charts.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved activity statistics"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/activity-stats")
    public ResponseEntity<Map<ActivityType, Long>> getActivityStats(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(dashboardService.getActivityStats(days));
    }
}
