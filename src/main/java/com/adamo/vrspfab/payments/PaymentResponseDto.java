package com.adamo.vrspfab.payments;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponseDto {
    private Long paymentId;
    private String transactionId;
    private String status;
    private String approvalUrl; // Only used for redirect flows like PayPal
}
