package com.adamo.vrspfab.payments;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.client.RestTemplate;


@Configuration
@ConditionalOnProperty(name = "payments.paypal.enabled", havingValue = "true")
public class PaypalConfig {

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.api.baseUrl}")
    private String paypalBaseUrl;

    /**
     * Creates a RestTemplate bean configured for PayPal API interactions.
     * @return RestTemplate instance configured with PayPal API settings.
     */
    @Bean
    public RestTemplate paypalRestTemplate() {
        return new RestTemplate();
    }
}
