package com.adamo.vrspfab.payments;


import lombok.Data;

@Data
public class DisputeRequestDto {
    private Long paymentId;
    private String reason;
    private String evidence;
}
