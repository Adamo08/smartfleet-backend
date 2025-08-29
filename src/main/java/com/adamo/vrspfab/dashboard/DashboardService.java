package com.adamo.vrspfab.dashboard;

import com.adamo.vrspfab.payments.PaymentRepository;
import com.adamo.vrspfab.payments.PaymentService;
import com.adamo.vrspfab.payments.PaymentStatus;
import com.adamo.vrspfab.reservations.ReservationRepository;
import com.adamo.vrspfab.reservations.ReservationService;
import com.adamo.vrspfab.users.UserService;
import com.adamo.vrspfab.vehicles.VehicleRepository;
import com.adamo.vrspfab.vehicles.VehicleService;
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
}
