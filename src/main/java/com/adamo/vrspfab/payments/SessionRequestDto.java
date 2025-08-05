package com.adamo.vrspfab.payments;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class SessionRequestDto {
    private Long reservationId;
    private long amount;
    private String currency;
    private String successUrl;
    private String cancelUrl;
}
