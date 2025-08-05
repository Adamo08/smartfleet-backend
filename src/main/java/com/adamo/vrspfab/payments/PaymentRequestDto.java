package com.adamo.vrspfab.payments;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequestDto {
    @NotNull
    private Long reservationId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String currency;

    @NotBlank
    private String paymentMethodId; // For direct processing (e.g., Stripe PaymentIntent)

    @NotBlank
    private String providerName; // e.g., "stripePaymentProvider"
}
