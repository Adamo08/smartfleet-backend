package com.adamo.vrspfab.dashboard;

import com.adamo.vrspfab.payments.PaymentService;
import com.adamo.vrspfab.reservations.ReservationService;
import com.adamo.vrspfab.users.UserService;
import com.adamo.vrspfab.vehicles.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserService userService;
    private final VehicleService vehicleService;
    private final ReservationService reservationService;
    private final PaymentService paymentService;

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        Long totalUsers = userService.countAllUsers();
        Long totalAdmins = userService.countUsersByRole("ADMIN");
        Long activeVehicles = vehicleService.countActiveVehicles();
        
        // TODO: Need methods in ReservationService to count reservations by status and total
        Long totalReservations = reservationService.countAllReservations();
        Long pendingReservations = reservationService.countReservationsByStatus("PENDING");
        Long completedReservations = reservationService.countReservationsByStatus("COMPLETED");
        Long cancelledReservations = reservationService.countReservationsByStatus("CANCELLED");

        // TODO: Need a method in PaymentService to get total revenue from completed payments
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
}
