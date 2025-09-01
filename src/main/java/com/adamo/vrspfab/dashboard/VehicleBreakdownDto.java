package com.adamo.vrspfab.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleBreakdownDto {
    private FleetOverview overview;
    private List<CategoryAnalytics> categories;
    private List<BrandAnalytics> brands;
    private List<StatusAnalytics> statuses;
    private List<ModelAnalytics> topModels;
    private UtilizationMetrics utilization;
    private RevenueMetrics revenue;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FleetOverview {
        private long totalVehicles;
        private long activeVehicles;
        private long availableVehicles;
        private long inUseVehicles;
        private long maintenanceVehicles;
        private double utilizationRate;
        private double availabilityRate;
        private int totalBrands;
        private int totalCategories;
        private int totalModels;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryAnalytics {
        private String name;
        private long totalVehicles;
        private long availableVehicles;
        private long rentedVehicles;
        private BigDecimal averagePrice;
        private BigDecimal totalRevenue;
        private double utilizationRate;
        private String iconUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandAnalytics {
        private String name;
        private long totalVehicles;
        private long availableVehicles;
        private long rentedVehicles;
        private BigDecimal averagePrice;
        private BigDecimal totalRevenue;
        private double averageRating;
        private int modelCount;
        private double marketShare;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusAnalytics {
        private String status;
        private long count;
        private double percentage;
        private String color;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelAnalytics {
        private String modelName;
        private String brandName;
        private String categoryName;
        private long totalVehicles;
        private long reservationCount;
        private BigDecimal totalRevenue;
        private double averageRating;
        private BigDecimal averagePrice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UtilizationMetrics {
        private double overallUtilization;
        private String mostUtilizedCategory;
        private String leastUtilizedCategory;
        private String mostUtilizedBrand;
        private String peakUsageHour;
        private List<HourlyUtilization> hourlyBreakdown;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyUtilization {
        private int hour;
        private long reservations;
        private double utilizationRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueMetrics {
        private BigDecimal totalRevenue;
        private BigDecimal averageRevenuePerVehicle;
        private String topRevenueCategory;
        private String topRevenueBrand;
        private BigDecimal monthlyGrowth;
        private List<CategoryRevenue> categoryRevenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryRevenue {
        private String categoryName;
        private BigDecimal revenue;
        private double percentage;
    }
}
