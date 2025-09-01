package com.adamo.vrspfab.payments;


import lombok.AllArgsConstructor;
import lombok.Data;


@AllArgsConstructor
@Data
public class SessionResponseDto {
    private String sessionId;
    private String checkoutUrl;
}
