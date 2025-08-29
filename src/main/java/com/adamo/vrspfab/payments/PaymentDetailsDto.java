package com.adamo.vrspfab.payments;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetailsDto {
    private Long id;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String provider;
    private String transactionId;
    private String captureId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Flat fields from related entities to avoid cycles
    private Long reservationId;
    private Long userId;
    private String userEmail;
}