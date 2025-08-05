package com.adamo.vrspfab.payments;


import lombok.AllArgsConstructor;
import lombok.Data;


@AllArgsConstructor
@Data
public class DisputeResponseDto {
    private Long disputeId;
    private DisputeStatus status;
}
