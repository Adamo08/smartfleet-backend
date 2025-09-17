package com.adamo.vrspfab.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDto {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;

    // Reservations
    private long totalReservations;
    private long pendingReservations;
    private long confirmedReservations;
    private long completedReservations;
    private long cancelledReservations;

    // Payments
    private long totalPayments;
    private long completedPayments;
    private long failedPayments;
    private BigDecimal totalSpent;

    // Refunds
    private long refundsCount;
    private BigDecimal totalRefunded;

    // Engagement
    private long favoritesCount;
    private long bookmarksCount;
    private long unreadNotifications;
}


