package com.adamo.vrspfab.payments;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RefundResponseDto {
    private Long refundRecordId; // The ID from our database
    private String refundTransactionId; // The ID from the payment provider
    private RefundStatus status;
}
