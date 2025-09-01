package com.adamo.vrspfab.dashboard;

import com.adamo.vrspfab.payments.PaymentRepository;
import com.adamo.vrspfab.payments.PaymentService;
import com.adamo.vrspfab.payments.PaymentStatus;
import com.adamo.vrspfab.reservations.ReservationRepository;
import com.adamo.vrspfab.reservations.ReservationService;
import com.adamo.vrspfab.users.UserService;
import com.adamo.vrspfab.vehicles.VehicleRepository;
import com.adamo.vrspfab.vehicles.VehicleService;
import com.adamo.vrspfab.vehicles.VehicleCategoryRepository;
import com.adamo.vrspfab.vehicles.VehicleBrandRepository;
import com.adamo.vrspfab.vehicles.VehicleStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserService userService;
    private final VehicleService vehicleService;
    private final ReservationService reservationService;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleCategoryRepository vehicleCategoryRepository;
    private final VehicleBrandRepository vehicleBrandRepository;
    private final ActivityService activityService;

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        Long totalUsers = userService.countAllUsers();
        Long totalAdmins = userService.countUsersByRole("ADMIN");
        Long activeVehicles = vehicleService.countActiveVehicles();
        
        Long totalReservations = reservationService.countAllReservations();
        Long pendingReservations = reservationService.countReservationsByStatus("PENDING");
        Long completedReservations = reservationService.countReservationsByStatus("COMPLETED");
        Long cancelledReservations = reservationService.countReservationsByStatus("CANCELLED");

        BigDecimal totalRevenue = paymentService.getTotalRevenue();

        return DashboardStatsDto.builder()
                .totalUsers(totalUsers)
                .totalAdmins(totalAdmins)
                .activeVehicles(activeVehicles)
                .totalReservations(totalReservations)
                .pendingReservations(pendingReservations)
                .completedReservations(completedReservations)
                .cancelledReservations(cancelledReservations)
                .totalRevenue(totalRevenue)
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardAnalyticsDto getDashboardAnalytics(int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        return DashboardAnalyticsDto.builder()
                .revenueData(getRevenueData(startDate, endDate))
                .vehicleUtilization(getVehicleUtilizationData())
                .monthlyPerformance(getMonthlyPerformanceData())
                .build();
    }

    private List<DashboardAnalyticsDto.RevenueDataPoint> getRevenueData(LocalDateTime startDate, LocalDateTime endDate) {
        List<DashboardAnalyticsDto.RevenueDataPoint> revenueData = new ArrayList<>();
        
        LocalDate currentDate = startDate.toLocalDate();
        LocalDate endLocalDate = endDate.toLocalDate();
        
        while (!currentDate.isAfter(endLocalDate)) {
            LocalDateTime dayStart = currentDate.atStartOfDay();
            LocalDateTime dayEnd = currentDate.plusDays(1).atStartOfDay();
            
            BigDecimal dailyRevenue = paymentRepository.sumAmountByStatusAndCreatedAtBetween(
                    PaymentStatus.COMPLETED, dayStart, dayEnd
            ).orElse(BigDecimal.ZERO);
            
            revenueData.add(DashboardAnalyticsDto.RevenueDataPoint.builder()
                    .date(currentDate)
                    .amount(dailyRevenue)
                    .build());
            
            currentDate = currentDate.plusDays(1);
        }
        
        return revenueData;
    }

    private List<DashboardAnalyticsDto.VehicleUtilizationData> getVehicleUtilizationData() {
        List<DashboardAnalyticsDto.VehicleUtilizationData> utilizationData = new ArrayList<>();
        
        // Get vehicle categories and their utilization
        List<Object[]> categoryUtilization = vehicleRepository.getVehicleUtilizationByCategory();
        
        for (Object[] row : categoryUtilization) {
            String categoryName = (String) row[0];
            Long totalVehicles = (Long) row[1];
            Long reservedVehicles = (Long) row[2];
            
            double utilizationRate = totalVehicles > 0 ? 
                    (reservedVehicles.doubleValue() / totalVehicles.doubleValue()) * 100 : 0.0;
            
            utilizationData.add(DashboardAnalyticsDto.VehicleUtilizationData.builder()
                    .vehicleType(categoryName)
                    .utilizationRate(Math.round(utilizationRate * 100.0) / 100.0)
                    .totalReservations(reservedVehicles)
                    .availableDays(totalVehicles - reservedVehicles)
                    .build());
        }
        
        return utilizationData;
    }

    private List<DashboardAnalyticsDto.MonthlyPerformanceData> getMonthlyPerformanceData() {
        List<DashboardAnalyticsDto.MonthlyPerformanceData> performanceData = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        
        // Get data for the last 6 months
        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = currentDate.minusMonths(i);
            LocalDateTime monthStart = monthDate.withDayOfMonth(1).atStartOfDay();
            LocalDateTime monthEnd = monthDate.withDayOfMonth(monthDate.lengthOfMonth()).atTime(23, 59, 59);
            
            Long monthlyReservations = reservationRepository.countByCreatedAtBetween(monthStart, monthEnd);
            BigDecimal monthlyRevenue = paymentRepository.sumAmountByStatusAndCreatedAtBetween(
                    PaymentStatus.COMPLETED, monthStart, monthEnd
            ).orElse(BigDecimal.ZERO);
            
            performanceData.add(DashboardAnalyticsDto.MonthlyPerformanceData.builder()
                    .month(monthDate.format(DateTimeFormatter.ofPattern("MMM")))
                    .reservationCount(monthlyReservations)
                    .revenue(monthlyRevenue)
                    .build());
        }
        
        return performanceData;
    }

    /**
     * Get activity statistics for the specified number of days
     */
    @Transactional(readOnly = true)
    public Map<ActivityType, Long> getActivityStats(int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        return activityService.getActivityStats(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public VehicleBreakdownDto getVehicleBreakdown() {
        // Fleet Overview
        long totalVehicles = vehicleRepository.count();
        long availableVehicles = vehicleRepository.countByStatus(VehicleStatus.AVAILABLE);
        long rentedVehicles = vehicleRepository.countByStatus(VehicleStatus.RENTED);
        long maintenanceVehicles = vehicleRepository.countByStatus(VehicleStatus.IN_MAINTENANCE);
        long outOfServiceVehicles = vehicleRepository.countByStatus(VehicleStatus.OUT_OF_SERVICE);
        
        int totalBrands = (int) vehicleBrandRepository.count();
        int totalCategories = (int) vehicleCategoryRepository.count();
        int totalModels = vehicleRepository.countDistinctModels();
        
        double utilizationRate = totalVehicles > 0 ? (double) rentedVehicles / totalVehicles * 100 : 0;
        double availabilityRate = totalVehicles > 0 ? (double) availableVehicles / totalVehicles * 100 : 0;
        
        VehicleBreakdownDto.FleetOverview overview = VehicleBreakdownDto.FleetOverview.builder()
                .totalVehicles(totalVehicles)
                .activeVehicles(totalVehicles - outOfServiceVehicles)
                .availableVehicles(availableVehicles)
                .inUseVehicles(rentedVehicles)
                .maintenanceVehicles(maintenanceVehicles)
                .utilizationRate(utilizationRate)
                .availabilityRate(availabilityRate)
                .totalBrands(totalBrands)
                .totalCategories(totalCategories)
                .totalModels(totalModels)
                .build();

        // Category Analytics with rich data
        List<VehicleBreakdownDto.CategoryAnalytics> categories = vehicleCategoryRepository.findAll().stream()
                .map(category -> {
                    long catTotal = vehicleRepository.countByCategoryId(category.getId());
                    long catAvailable = vehicleRepository.countByCategoryIdAndStatus(category.getId(), VehicleStatus.AVAILABLE);
                    long catRented = vehicleRepository.countByCategoryIdAndStatus(category.getId(), VehicleStatus.RENTED);
                    BigDecimal avgPrice = vehicleRepository.getAveragePriceByCategoryId(category.getId());
                    BigDecimal catRevenue = paymentRepository.getTotalRevenueByCategoryId(category.getId());
                    double catUtilization = catTotal > 0 ? (double) catRented / catTotal * 100 : 0;
                    
                    return VehicleBreakdownDto.CategoryAnalytics.builder()
                            .name(category.getName())
                            .totalVehicles(catTotal)
                            .availableVehicles(catAvailable)
                            .rentedVehicles(catRented)
                            .averagePrice(avgPrice != null ? avgPrice : BigDecimal.ZERO)
                            .totalRevenue(catRevenue != null ? catRevenue : BigDecimal.ZERO)
                            .utilizationRate(catUtilization)
                            .iconUrl(category.getIconUrl())
                            .build();
                })
                .filter(cat -> cat.getTotalVehicles() > 0)
                .toList();

        // Brand Analytics with market share
        List<VehicleBreakdownDto.BrandAnalytics> brands = vehicleBrandRepository.findAll().stream()
                .map(brand -> {
                    long brandTotal = vehicleRepository.countByBrandId(brand.getId());
                    long brandAvailable = vehicleRepository.countByBrandIdAndStatus(brand.getId(), VehicleStatus.AVAILABLE);
                    long brandRented = vehicleRepository.countByBrandIdAndStatus(brand.getId(), VehicleStatus.RENTED);
                    BigDecimal avgPrice = vehicleRepository.getAveragePriceByBrandId(brand.getId());
                    BigDecimal brandRevenue = paymentRepository.getTotalRevenueByBrandId(brand.getId());
                    int modelCount = vehicleRepository.countDistinctModelsByBrandId(brand.getId());
                    double marketShare = totalVehicles > 0 ? (double) brandTotal / totalVehicles * 100 : 0;
                    Double avgRatingObj = vehicleRepository.getAverageRatingByBrandId(brand.getId());
                    double avgRating = avgRatingObj != null ? avgRatingObj : 4.2;
                    return VehicleBreakdownDto.BrandAnalytics.builder()
                            .name(brand.getName())
                            .totalVehicles(brandTotal)
                            .availableVehicles(brandAvailable)
                            .rentedVehicles(brandRented)
                            .averagePrice(avgPrice != null ? avgPrice : BigDecimal.ZERO)
                            .totalRevenue(brandRevenue != null ? brandRevenue : BigDecimal.ZERO)
                            .averageRating(avgRating)
                            .modelCount(modelCount)
                            .marketShare(marketShare)
                            .build();
                })
                .filter(brand -> brand.getTotalVehicles() > 0)
                .toList();

        // Status Analytics with colors and descriptions
        List<VehicleBreakdownDto.StatusAnalytics> statuses = List.of(
                VehicleBreakdownDto.StatusAnalytics.builder()
                        .status(VehicleStatus.AVAILABLE.name())
                        .count(availableVehicles)
                        .percentage(totalVehicles > 0 ? (double) availableVehicles / totalVehicles * 100 : 0)
                        .color("#10B981")
                        .description("Ready for booking")
                        .build(),
                VehicleBreakdownDto.StatusAnalytics.builder()
                        .status(VehicleStatus.RENTED.name())
                        .count(rentedVehicles)
                        .percentage(totalVehicles > 0 ? (double) rentedVehicles / totalVehicles * 100 : 0)
                        .color("#3B82F6")
                        .description("Currently rented out")
                        .build(),
                VehicleBreakdownDto.StatusAnalytics.builder()
                        .status(VehicleStatus.IN_MAINTENANCE.name())
                        .count(maintenanceVehicles)
                        .percentage(totalVehicles > 0 ? (double) maintenanceVehicles / totalVehicles * 100 : 0)
                        .color("#F59E0B")
                        .description("Under maintenance")
                        .build(),
                VehicleBreakdownDto.StatusAnalytics.builder()
                        .status(VehicleStatus.OUT_OF_SERVICE.name())
                        .count(outOfServiceVehicles)
                        .percentage(totalVehicles > 0 ? (double) outOfServiceVehicles / totalVehicles * 100 : 0)
                        .color("#EF4444")
                        .description("Out of service")
                        .build()
        );

        // Top Models Analytics
        List<VehicleBreakdownDto.ModelAnalytics> topModels = vehicleRepository.getTopPerformingModels()
                .stream()
                .limit(10)
                .map(result -> {
                    // Handle different number types that Hibernate might return
                    BigDecimal totalRevenue = BigDecimal.ZERO;
                    BigDecimal averagePrice = BigDecimal.ZERO;
                    
                    if (result[5] != null) {
                        if (result[5] instanceof BigDecimal) {
                            totalRevenue = (BigDecimal) result[5];
                        } else if (result[5] instanceof Number) {
                            totalRevenue = BigDecimal.valueOf(((Number) result[5]).doubleValue());
                        }
                    }
                    
                    if (result[7] != null) {
                        if (result[7] instanceof BigDecimal) {
                            averagePrice = (BigDecimal) result[7];
                        } else if (result[7] instanceof Number) {
                            averagePrice = BigDecimal.valueOf(((Number) result[7]).doubleValue());
                        }
                    }
                    
                    return VehicleBreakdownDto.ModelAnalytics.builder()
                            .modelName((String) result[0])
                            .brandName((String) result[1])
                            .categoryName((String) result[2])
                            .totalVehicles(((Number) result[3]).longValue())
                            .reservationCount(((Number) result[4]).longValue())
                            .totalRevenue(totalRevenue)
                            .averageRating(((Number) result[6]).doubleValue())
                            .averagePrice(averagePrice)
                            .build();
                })
                .toList();

        // Utilization Metrics
        String mostUtilizedCat = categories.stream()
                .max((c1, c2) -> Double.compare(c1.getUtilizationRate(), c2.getUtilizationRate()))
                .map(VehicleBreakdownDto.CategoryAnalytics::getName)
                .orElse("N/A");
                
        String leastUtilizedCat = categories.stream()
                .min((c1, c2) -> Double.compare(c1.getUtilizationRate(), c2.getUtilizationRate()))
                .map(VehicleBreakdownDto.CategoryAnalytics::getName)
                .orElse("N/A");

        String mostUtilizedBrand = brands.stream()
                .max((b1, b2) -> Double.compare(b1.getMarketShare(), b2.getMarketShare()))
                .map(VehicleBreakdownDto.BrandAnalytics::getName)
                .orElse("N/A");

        // Get hourly breakdown from real data
        List<VehicleBreakdownDto.HourlyUtilization> hourlyBreakdown;
        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            hourlyBreakdown = reservationRepository.getHourlyUtilizationData(thirtyDaysAgo)
                    .stream()
                    .map(result -> VehicleBreakdownDto.HourlyUtilization.builder()
                            .hour((Integer) result[0])
                            .reservations((Long) result[1])
                            .utilizationRate((Double) result[2])
                            .build())
                    .toList();
        } catch (Exception e) {
            // Fallback to empty list if query fails
            hourlyBreakdown = new ArrayList<>();
            for (int hour = 0; hour < 24; hour++) {
                hourlyBreakdown.add(VehicleBreakdownDto.HourlyUtilization.builder()
                        .hour(hour)
                        .reservations(0L)
                        .utilizationRate(0.0)
                        .build());
            }
        }

        VehicleBreakdownDto.UtilizationMetrics utilization = VehicleBreakdownDto.UtilizationMetrics.builder()
                .overallUtilization(utilizationRate)
                .mostUtilizedCategory(mostUtilizedCat)
                .leastUtilizedCategory(leastUtilizedCat)
                .mostUtilizedBrand(mostUtilizedBrand)
                .peakUsageHour("14:00") // Can be calculated from real data
                .hourlyBreakdown(hourlyBreakdown)
                .build();

        // Revenue Metrics
        BigDecimal totalRevenue = paymentRepository.getTotalFleetRevenue();
        BigDecimal avgRevenuePerVehicle = totalVehicles > 0 && totalRevenue != null ? 
                totalRevenue.divide(BigDecimal.valueOf(totalVehicles), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
        
        String topRevenueCategory = categories.stream()
                .max((c1, c2) -> c1.getTotalRevenue().compareTo(c2.getTotalRevenue()))
                .map(VehicleBreakdownDto.CategoryAnalytics::getName)
                .orElse("N/A");
                
        String topRevenueBrand = brands.stream()
                .max((b1, b2) -> b1.getTotalRevenue().compareTo(b2.getTotalRevenue()))
                .map(VehicleBreakdownDto.BrandAnalytics::getName)
                .orElse("N/A");

        List<VehicleBreakdownDto.CategoryRevenue> categoryRevenue = categories.stream()
                .map(cat -> {
                    double percentage = totalRevenue != null && totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                            cat.getTotalRevenue().divide(totalRevenue, 4, BigDecimal.ROUND_HALF_UP).doubleValue() * 100 : 0;
                    return VehicleBreakdownDto.CategoryRevenue.builder()
                            .categoryName(cat.getName())
                            .revenue(cat.getTotalRevenue())
                            .percentage(percentage)
                            .build();
                })
                .toList();

        VehicleBreakdownDto.RevenueMetrics revenue = VehicleBreakdownDto.RevenueMetrics.builder()
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .averageRevenuePerVehicle(avgRevenuePerVehicle)
                .topRevenueCategory(topRevenueCategory)
                .topRevenueBrand(topRevenueBrand)
                .monthlyGrowth(paymentRepository.getMonthlyGrowthRate())
                .categoryRevenue(categoryRevenue)
                .build();

        return VehicleBreakdownDto.builder()
                .overview(overview)
                .categories(categories)
                .brands(brands)
                .statuses(statuses)
                .topModels(topModels)
                .utilization(utilization)
                .revenue(revenue)
                .build();
    }
}
