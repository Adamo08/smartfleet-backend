package com.adamo.vrspfab.payments;


import lombok.Data;

@Data
public class DisputeResponseDto {
    private Long disputeId;
    private DisputeStatus status;
}
