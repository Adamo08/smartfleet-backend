package com.adamo.vrspfab.payments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodsDto {
    private List<PaymentMethodDto> methods;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentMethodDto {
        private String id;
        private String name;
        private String description;
        private String icon;
        private boolean isActive;
    }
}

