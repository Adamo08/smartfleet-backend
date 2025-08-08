package com.adamo.vrspfab.notifications;

import com.adamo.vrspfab.common.SecurityRules;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class NotificationSecurityRules implements SecurityRules {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        registry
                // Admin-specific endpoints
                .requestMatchers(HttpMethod.POST, "/notifications/broadcast").hasRole("ADMIN")
                // General user endpoints
                .requestMatchers("/notifications/**").authenticated();
    }
}