package com.adamo.vrspfab.payments;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundRequestDto {
    @NotNull(message = "Payment ID cannot be null")
    private Long paymentId;
    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    private String reason;  // Optional reason for the refund
}