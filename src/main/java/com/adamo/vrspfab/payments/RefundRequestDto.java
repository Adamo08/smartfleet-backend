package com.adamo.vrspfab.payments;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class RefundRequestDto {
    private Long paymentId;
    private long amount;
    private String reason;
}
