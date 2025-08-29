package com.adamo.vrspfab.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAnalyticsDto {
    
    // Revenue data for charts
    private List<RevenueDataPoint> revenueData;
    
    // Vehicle utilization data
    private List<VehicleUtilizationData> vehicleUtilization;
    
    // Monthly performance data
    private List<MonthlyPerformanceData> monthlyPerformance;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueDataPoint {
        private LocalDate date;
        private BigDecimal amount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleUtilizationData {
        private String vehicleType;
        private Double utilizationRate;
        private Long totalReservations;
        private Long availableDays;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyPerformanceData {
        private String month;
        private Long reservationCount;
        private BigDecimal revenue;
    }
}
