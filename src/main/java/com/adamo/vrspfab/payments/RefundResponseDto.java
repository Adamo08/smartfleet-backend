package com.adamo.vrspfab.payments;


import lombok.AllArgsConstructor;
import lombok.Data;


@AllArgsConstructor
@Data
public class RefundResponseDto {
    private Long refundId;
    private RefundStatus status;
}
