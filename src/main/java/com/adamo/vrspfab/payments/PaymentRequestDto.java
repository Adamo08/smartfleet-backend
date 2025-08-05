package com.adamo.vrspfab.payments;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequestDto {
    private Long reservationId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethodId;
}
