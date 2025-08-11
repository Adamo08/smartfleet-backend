package com.adamo.vrspfab.payments;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundDetailsDto {
    private Long id;
    private Long paymentId;
    private String refundTransactionId;
    private BigDecimal amount;
    private String currency;
    private String reason;
    private RefundStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private Long reservationId;
    private Long userId;
    private String userEmail;
}