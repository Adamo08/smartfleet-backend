package com.adamo.vrspfab.payments;


import lombok.Data;


@Data
public class PaymentRequestDto {
    private Long reservationId;
    private long amount;
    private String currency;
    private String paymentMethodId;
}
