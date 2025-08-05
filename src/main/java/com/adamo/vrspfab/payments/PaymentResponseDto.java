package com.adamo.vrspfab.payments;


import lombok.Data;

@Data
public class PaymentResponseDto {
    private Long paymentId;
    private String transactionId;
    private String status;
    private String approvalUrl;
}
