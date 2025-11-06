package com.adamo.vrspfab.common.config;

import com.adamo.vrspfab.payments.PaymentProvider;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
public class TestPaypalConfig {

    @Bean("paypalRestTemplate")
    @Primary
    public RestTemplate paypalRestTemplate() {
        return new RestTemplate();
    }

    @Bean("paypalPaymentProvider")
    @Primary
    public PaymentProvider paypalPaymentProvider() {
        return Mockito.mock(PaymentProvider.class);
    }
}