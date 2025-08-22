package com.adamo.vrspfab.payments;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentFilter {
    private Long userId;
    private Long reservationId;
    private PaymentStatus status;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
