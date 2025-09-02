package com.adamo.vrspfab.notifications;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class EmailConfig {

    /**
     * Creates a RestTemplate bean for SendPulse email service.
     * @return RestTemplate instance configured for email API calls.
     */
    @Bean("emailRestTemplate")
    public RestTemplate emailRestTemplate() {
        return new RestTemplate();
    }
}
