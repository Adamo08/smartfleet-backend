package com.adamo.vrspfab.common;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class CommonSecurityRules implements SecurityRules{
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        registry
                // Publicly accessible endpoint for enums and home
                .requestMatchers("/api/enums/**").permitAll()
                .requestMatchers("/home").permitAll()
                
                // Admin-only access for admin panel endpoints
                .requestMatchers("/admin/**").hasRole("ADMIN");
    }
}
