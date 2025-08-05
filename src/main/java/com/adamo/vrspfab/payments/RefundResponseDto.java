package com.adamo.vrspfab.payments;


import lombok.Data;

@Data
public class RefundResponseDto {
    private Long refundId;
    private RefundStatus status;
}
