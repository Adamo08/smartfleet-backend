package com.adamo.vrspfab.common.config;

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
}