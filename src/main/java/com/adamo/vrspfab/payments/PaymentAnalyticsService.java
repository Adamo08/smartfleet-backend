package com.adamo.vrspfab.payments;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentAnalyticsService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;

    @Transactional(readOnly = true)
    public AnalyticsReportDto getPaymentAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        AnalyticsReportDto report = new AnalyticsReportDto();

        long totalPayments = paymentRepository.countByCreatedAtBetween(startDate, endDate);
        long successfulPayments = paymentRepository.countByStatusAndCreatedAtBetween(
                PaymentStatus.COMPLETED, startDate, endDate);
        BigDecimal totalAmount = paymentRepository.sumAmountByStatusAndCreatedAtBetween(
                PaymentStatus.COMPLETED, startDate, endDate).orElse(BigDecimal.ZERO);
        long totalRefunds = refundRepository.countByProcessedAtBetween(startDate, endDate);

        report.setTotalPayments(totalPayments);
        report.setSuccessfulPayments(successfulPayments);
        report.setTotalRevenue(totalAmount);
        report.setTotalRefunds(totalRefunds);

        double successRate = (totalPayments > 0) ? ((double) successfulPayments / totalPayments) * 100 : 0.0;
        report.setSuccessRate(successRate);

        double refundRate = (totalPayments > 0) ? ((double) totalRefunds / totalPayments) * 100 : 0.0;
        report.setRefundRate(refundRate);

        BigDecimal averagePayment = (successfulPayments > 0)
                ? totalAmount.divide(BigDecimal.valueOf(successfulPayments), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        report.setAveragePaymentAmount(averagePayment);

        return report;
    }
}
