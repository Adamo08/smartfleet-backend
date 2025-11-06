package com.adamo.vrspfab.common.config;

import com.adamo.vrspfab.payments.PaymentProvider;
import com.adamo.vrspfab.payments.PaypalPaymentProvider;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
public class IntegrationTestConfig {

    @Bean("paypalRestTemplate")
    @Primary
    public RestTemplate paypalRestTemplate() {
        return new RestTemplate();
    }

    @Bean("paypalPaymentProvider")
    @Primary
    public PaypalPaymentProvider paypalPaymentProvider() {
        return Mockito.mock(PaypalPaymentProvider.class);
    }

    // No webhook processor mock here: we'll keep test config minimal. If specific tests need it,
    // add a dedicated TestConfiguration in that test package.
}