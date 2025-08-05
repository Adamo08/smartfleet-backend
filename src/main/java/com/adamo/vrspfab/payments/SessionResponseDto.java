package com.adamo.vrspfab.payments;


import lombok.Data;

@Data
public class SessionResponseDto {
    private String sessionId;
    private String checkoutUrl;
}
