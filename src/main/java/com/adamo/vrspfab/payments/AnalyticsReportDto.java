package com.adamo.vrspfab.payments;

import lombok.Data;

@Data
public class AnalyticsReportDto {
    private double successRate;
    private long totalPayments;
    private long totalDisputes;
    private double averagePaymentAmount;
}