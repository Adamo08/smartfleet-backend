package com.adamo.vrspfab.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private Long totalReservations;
    private Long activeVehicles;
    private BigDecimal totalRevenue;
    private Long totalUsers;
    private Long totalAdmins;
    private Long pendingReservations;
    private Long completedReservations;
    private Long cancelledReservations;
}
