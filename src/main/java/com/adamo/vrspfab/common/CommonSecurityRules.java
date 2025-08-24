package com.adamo.vrspfab.common;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class CommonSecurityRules implements SecurityRules{
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        registry
                // Publicly accessible endpoint for enums
                .requestMatchers("/api/enums/**").permitAll();
    }
}
