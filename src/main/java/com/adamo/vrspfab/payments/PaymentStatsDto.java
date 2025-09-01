package com.adamo.vrspfab.payments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentStatsDto {
    private int totalPayments;
    private BigDecimal totalAmount;
    private int pendingPayments;
    private int completedPayments;
    private int failedPayments;
}

