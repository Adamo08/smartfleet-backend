package com.adamo.vrspfab.payments;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AnalyticsReportDto {
    private long totalPayments;
    private long successfulPayments;
    private double successRate;
    private long totalRefunds;
    private double refundRate;
    private BigDecimal totalRevenue;
    private BigDecimal averagePaymentAmount;
}
