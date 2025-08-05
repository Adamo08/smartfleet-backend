package com.adamo.vrspfab.payments;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SessionRequestDto {
    @NotNull
    private Long reservationId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String currency;

    @NotBlank
    private String successUrl;

    @NotBlank
    private String cancelUrl;

    @NotBlank
    private String providerName; // e.g., "stripePaymentProvider"
}