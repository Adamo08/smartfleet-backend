package com.adamo.vrspfab.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class WebConfig {
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter f = new CommonsRequestLoggingFilter();
        f.setIncludeClientInfo(true);
        f.setIncludeQueryString(true);
        f.setIncludePayload(false);
        f.setMaxPayloadLength(10000);
        f.setBeforeMessagePrefix("REQUEST DATA : ");
        f.setAfterMessagePrefix("REQUEST END : ");
        return f;
    }
}
