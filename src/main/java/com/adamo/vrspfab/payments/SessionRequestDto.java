package com.adamo.vrspfab.payments;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SessionRequestDto {
    @NotNull (message = "Reservation ID cannot be null")
    private Long reservationId;

    @NotNull (message = "Amount cannot be null")
    @Positive (message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank (message = "Currency cannot be blank")
    private String currency;

    @NotBlank (message = "Success URL cannot be blank")
    private String successUrl;

    @NotBlank (message = "Cancel URL cannot be blank")
    private String cancelUrl;

    @NotBlank (message = "Provider name cannot be blank")
    private String providerName; // e.g., "paypalPaymentProvider"
}